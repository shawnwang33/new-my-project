git add .
git commit -m "update"
git push

#重新运行bert推荐python脚本
python web-project/web-recommend/scripts/build_personalized_recommend.py --host 127.0.0.1 --port 3307 --user root --password 123456 --database web --top-n 50
