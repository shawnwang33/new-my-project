#!/usr/bin/env python
"""
离线生成个性化推荐结果:
1) 新闻标题 -> BERT 向量(news_embedding)
2) 用户行为聚合 -> 用户兴趣向量(user_profile)
3) 余弦相似度排序 -> 推荐列表(user_recommend)

依赖:
  pip install sentence-transformers pymysql numpy
"""
from __future__ import annotations

import argparse
import json
import math
from collections import defaultdict
from dataclasses import dataclass
from datetime import datetime, timezone
from typing import Dict, List, Sequence, Tuple

import numpy as np
import pymysql
from pymysql.cursors import DictCursor
from sentence_transformers import SentenceTransformer


@dataclass
class NewsItem:
    news_id: int
    title: str
    category_id: int
    view_count: int
    publish_time: datetime | None


@dataclass
class Behavior:
    user_id: int
    news_id: int
    action_type: str
    action_time: datetime | None


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Build personalized recommend results with BERT embeddings.")
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=3307)
    parser.add_argument("--user", default="root")
    parser.add_argument("--password", default="123456")
    parser.add_argument("--database", default="web")
    parser.add_argument("--charset", default="utf8mb4")
    parser.add_argument("--model-name", default="shibing624/text2vec-base-chinese")
    parser.add_argument("--top-n", type=int, default=50)
    parser.add_argument("--browse-weight", type=float, default=1.0)
    parser.add_argument("--collect-weight", type=float, default=3.0)
    parser.add_argument("--time-decay-lambda", type=float, default=0.08, help="days decay lambda")
    return parser.parse_args()


def to_utc_now() -> datetime:
    return datetime.now(timezone.utc).replace(tzinfo=None)


def cosine_similarity(vec_a: np.ndarray, vec_b: np.ndarray) -> float:
    denom = (np.linalg.norm(vec_a) * np.linalg.norm(vec_b))
    if denom <= 0:
        return 0.0
    return float(np.dot(vec_a, vec_b) / denom)


def normalize_score(value: float, min_val: float, max_val: float) -> float:
    if max_val - min_val <= 1e-12:
        return 0.0
    return (value - min_val) / (max_val - min_val)


def load_news(cursor) -> List[NewsItem]:
    cursor.execute(
        """
        select id, title, category_id, view_count, publish_time
        from news_article
        where status = 1
        """
    )
    rows = cursor.fetchall()
    return [
        NewsItem(
            news_id=int(row["id"]),
            title=(row["title"] or "").strip(),
            category_id=int(row["category_id"]) if row["category_id"] is not None else 0,
            view_count=int(row["view_count"] or 0),
            publish_time=row["publish_time"],
        )
        for row in rows
    ]


def load_users(cursor) -> List[int]:
    cursor.execute("select userId from `user`")
    return [int(row["userId"]) for row in cursor.fetchall()]


def load_behaviors(cursor) -> List[Behavior]:
    cursor.execute(
        """
        select b.userId as user_id, b.newsId as news_id, 'browse' as action_type, b.browseTime as action_time
        from browse b
        union all
        select c.userId as user_id, c.newsId as news_id, 'collect' as action_type, c.collectTime as action_time
        from collect c
        """
    )
    rows = cursor.fetchall()
    return [
        Behavior(
            user_id=int(row["user_id"]),
            news_id=int(row["news_id"]),
            action_type=row["action_type"],
            action_time=row["action_time"],
        )
        for row in rows
    ]


def build_news_embeddings(model: SentenceTransformer, news_list: Sequence[NewsItem]) -> Dict[int, np.ndarray]:
    titles = [item.title if item.title else f"新闻{item.news_id}" for item in news_list]
    vectors = model.encode(titles, batch_size=64, show_progress_bar=True, normalize_embeddings=False)
    return {item.news_id: np.asarray(vectors[idx], dtype=np.float32) for idx, item in enumerate(news_list)}


def behavior_weight(action_type: str, action_time: datetime | None, now_dt: datetime, browse_weight: float,
                    collect_weight: float, time_decay_lambda: float) -> float:
    base = collect_weight if action_type == "collect" else browse_weight
    if action_time is None:
        return base
    days = max((now_dt - action_time).total_seconds() / 86400.0, 0.0)
    decay = math.exp(-time_decay_lambda * days)
    return base * decay


def aggregate_user_profile(
    users: Sequence[int],
    behaviors: Sequence[Behavior],
    news_vec_map: Dict[int, np.ndarray],
    browse_weight: float,
    collect_weight: float,
    time_decay_lambda: float,
) -> Tuple[Dict[int, np.ndarray], Dict[int, Dict[int, float]], Dict[int, set]]:
    now_dt = datetime.now()
    user_weighted_vecs: Dict[int, np.ndarray] = {}
    user_category_pref: Dict[int, Dict[int, float]] = defaultdict(lambda: defaultdict(float))
    user_interacted: Dict[int, set] = defaultdict(set)

    tmp_vec_sum: Dict[int, np.ndarray] = {}
    tmp_weight_sum: Dict[int, float] = defaultdict(float)

    for bh in behaviors:
        vec = news_vec_map.get(bh.news_id)
        if vec is None:
            continue
        w = behavior_weight(
            action_type=bh.action_type,
            action_time=bh.action_time,
            now_dt=now_dt,
            browse_weight=browse_weight,
            collect_weight=collect_weight,
            time_decay_lambda=time_decay_lambda,
        )
        if w <= 0:
            continue
        if bh.user_id not in tmp_vec_sum:
            tmp_vec_sum[bh.user_id] = np.zeros_like(vec, dtype=np.float32)
        tmp_vec_sum[bh.user_id] += vec * w
        tmp_weight_sum[bh.user_id] += w
        user_interacted[bh.user_id].add(bh.news_id)

    news_category_map = {}
    for n_id, vec in news_vec_map.items():
        _ = vec
        news_category_map[n_id] = 0
    # category 偏好从行为聚合，避免依赖额外 SQL
    for bh in behaviors:
        if bh.news_id not in news_vec_map:
            continue
        w = behavior_weight(
            action_type=bh.action_type,
            action_time=bh.action_time,
            now_dt=now_dt,
            browse_weight=browse_weight,
            collect_weight=collect_weight,
            time_decay_lambda=time_decay_lambda,
        )
        user_category_pref[bh.user_id][bh.news_id] += w

    for user_id in users:
        if tmp_weight_sum[user_id] > 0:
            user_weighted_vecs[user_id] = tmp_vec_sum[user_id] / tmp_weight_sum[user_id]

    return user_weighted_vecs, user_category_pref, user_interacted


def build_category_pref_by_user(
    users: Sequence[int], behaviors: Sequence[Behavior], news_map: Dict[int, NewsItem], browse_weight: float,
    collect_weight: float, time_decay_lambda: float
) -> Dict[int, Dict[int, float]]:
    now_dt = datetime.now()
    pref: Dict[int, Dict[int, float]] = defaultdict(lambda: defaultdict(float))
    for bh in behaviors:
        news = news_map.get(bh.news_id)
        if news is None:
            continue
        w = behavior_weight(
            action_type=bh.action_type,
            action_time=bh.action_time,
            now_dt=now_dt,
            browse_weight=browse_weight,
            collect_weight=collect_weight,
            time_decay_lambda=time_decay_lambda,
        )
        pref[bh.user_id][news.category_id] += w
    for user_id in users:
        _ = pref[user_id]
    return pref


def save_news_embedding(cursor, news_vec_map: Dict[int, np.ndarray], model_name: str, updated_at: datetime) -> None:
    rows = []
    for news_id, vec in news_vec_map.items():
        rows.append(
            (
                news_id,
                json.dumps([round(float(v), 6) for v in vec], ensure_ascii=False),
                int(vec.shape[0]),
                model_name,
                updated_at,
            )
        )
    cursor.executemany(
        """
        insert into news_embedding(news_id, vec, dim, model_name, updated_at)
        values(%s, %s, %s, %s, %s)
        on duplicate key update
          vec = values(vec),
          dim = values(dim),
          model_name = values(model_name),
          updated_at = values(updated_at)
        """,
        rows,
    )


def save_user_profile(cursor, user_vec_map: Dict[int, np.ndarray], behavior_count: Dict[int, int], updated_at: datetime) -> None:
    if not user_vec_map:
        return
    rows = []
    for user_id, vec in user_vec_map.items():
        rows.append(
            (
                user_id,
                json.dumps([round(float(v), 6) for v in vec], ensure_ascii=False),
                int(vec.shape[0]),
                int(behavior_count.get(user_id, 0)),
                updated_at,
            )
        )
    cursor.executemany(
        """
        insert into user_profile(user_id, vec, dim, behavior_count, updated_at)
        values(%s, %s, %s, %s, %s)
        on duplicate key update
          vec = values(vec),
          dim = values(dim),
          behavior_count = values(behavior_count),
          updated_at = values(updated_at)
        """,
        rows,
    )


def save_user_recommend(cursor, user_recommend_rows: List[Tuple[int, int, float, int, str, datetime]]) -> None:
    if not user_recommend_rows:
        return
    cursor.executemany(
        """
        insert into user_recommend(user_id, news_id, score, rank_no, reason, updated_at)
        values(%s, %s, %s, %s, %s, %s)
        on duplicate key update
          score = values(score),
          rank_no = values(rank_no),
          reason = values(reason),
          updated_at = values(updated_at)
        """,
        user_recommend_rows,
    )


def build_personalized_recommend(
    users: Sequence[int],
    news_list: Sequence[NewsItem],
    news_vec_map: Dict[int, np.ndarray],
    user_vec_map: Dict[int, np.ndarray],
    user_category_pref: Dict[int, Dict[int, float]],
    user_interacted: Dict[int, set],
    top_n: int,
    updated_at: datetime,
) -> List[Tuple[int, int, float, int, str, datetime]]:
    if not news_list:
        return []

    view_counts = [item.view_count for item in news_list]
    min_view, max_view = min(view_counts), max(view_counts)

    all_rows: List[Tuple[int, int, float, int, str, datetime]] = []

    for user_id in users:
        interacted = user_interacted.get(user_id, set())
        candidates = [item for item in news_list if item.news_id not in interacted]
        if not candidates:
            continue

        category_pref = user_category_pref.get(user_id, {})
        category_weight_sum = sum(category_pref.values())
        category_pref_ratio = {}
        if category_weight_sum > 0:
            for cid, val in category_pref.items():
                category_pref_ratio[cid] = val / category_weight_sum

        ranked: List[Tuple[int, float, str]] = []
        user_vec = user_vec_map.get(user_id)

        for item in candidates:
            hot_score = normalize_score(item.view_count, min_view, max_view)
            category_bonus = category_pref_ratio.get(item.category_id, 0.0)
            if user_vec is not None:
                news_vec = news_vec_map[item.news_id]
                sim_score = cosine_similarity(user_vec, news_vec)
                final_score = 0.75 * sim_score + 0.20 * hot_score + 0.05 * category_bonus
                reason = "personalized"
            else:
                # 冷启动: 热门新闻 + 分类偏好(无行为用户默认无类别偏好，保留兼容字段)
                final_score = 0.90 * hot_score + 0.10 * category_bonus
                reason = "cold_start"
            ranked.append((item.news_id, float(final_score), reason))

        ranked.sort(key=lambda x: x[1], reverse=True)
        for idx, (news_id, score, reason) in enumerate(ranked[:top_n], start=1):
            all_rows.append((user_id, news_id, score, idx, reason, updated_at))

    return all_rows


def main() -> None:
    args = parse_args()
    print("[1/5] load model:", args.model_name)
    model = SentenceTransformer(args.model_name)

    print("[2/5] load db data")
    conn = pymysql.connect(
        host=args.host,
        port=args.port,
        user=args.user,
        password=args.password,
        database=args.database,
        charset=args.charset,
        cursorclass=DictCursor,
        autocommit=False,
    )
    try:
        with conn.cursor() as cursor:
            news_list = load_news(cursor)
            users = load_users(cursor)
            behaviors = load_behaviors(cursor)

        if not news_list:
            print("no active news found, skip")
            return

        print(f"[3/5] build news embeddings: news={len(news_list)}")
        news_vec_map = build_news_embeddings(model, news_list)
        news_map = {item.news_id: item for item in news_list}
        behavior_count = defaultdict(int)
        for b in behaviors:
            behavior_count[b.user_id] += 1

        print("[4/5] build user profile and recommendation")
        user_vec_map, _, user_interacted = aggregate_user_profile(
            users=users,
            behaviors=behaviors,
            news_vec_map=news_vec_map,
            browse_weight=args.browse_weight,
            collect_weight=args.collect_weight,
            time_decay_lambda=args.time_decay_lambda,
        )
        user_category_pref = build_category_pref_by_user(
            users=users,
            behaviors=behaviors,
            news_map=news_map,
            browse_weight=args.browse_weight,
            collect_weight=args.collect_weight,
            time_decay_lambda=args.time_decay_lambda,
        )
        updated_at = to_utc_now()

        rec_rows = build_personalized_recommend(
            users=users,
            news_list=news_list,
            news_vec_map=news_vec_map,
            user_vec_map=user_vec_map,
            user_category_pref=user_category_pref,
            user_interacted=user_interacted,
            top_n=args.top_n,
            updated_at=updated_at,
        )

        print("[5/5] write results to db")
        with conn.cursor() as cursor:
            save_news_embedding(cursor, news_vec_map, args.model_name, updated_at)
            save_user_profile(cursor, user_vec_map, behavior_count, updated_at)
            cursor.execute("delete from user_recommend")
            save_user_recommend(cursor, rec_rows)
        conn.commit()
        print(f"done: users={len(users)}, user_profile={len(user_vec_map)}, user_recommend={len(rec_rows)}")
    finally:
        conn.close()


if __name__ == "__main__":
    main()
