# 普通镜像构建，随系统版本构建 amd/arm
docker build --load -t ywz626/ywz-ai-agent-station-study-app:1.0 -f ./Dockerfile .

# 兼容 amd、arm 构建镜像
# docker buildx build --l oad --platform liunx/amd64,linux/arm64 -t xiaofuge/xfg-frame-archetype-app:1.0 -f ./Dockerfile . --push