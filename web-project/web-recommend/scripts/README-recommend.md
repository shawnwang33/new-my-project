个性化推荐离线任务

1. 初始化数据库
执行 SQL 文件: web-project/web-recommend/sql/recommend_dl_init.sql

2. 安装依赖
pip install -r web-project/web-recommend/scripts/requirements-recommend.txt

3. 运行离线任务
python web-project/web-recommend/scripts/build_personalized_recommend.py --host 127.0.0.1 --port 3307 --user root --password 123456 --database web --top-n 50

默认模型: shibing624/text2vec-base-chinese
默认权重: collect=3, browse=1
默认时间衰减: exp(-0.08 * days)

4. 接口行为
/api/news/recommend 有 userId 且 user_recommend 有数据时优先返回个性化结果, 否则回退原推荐逻辑
