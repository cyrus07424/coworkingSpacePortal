#!/bin/bash

# Slack通知テストスクリプト
# このスクリプトはSlack通知機能をテストするためのものです
# 実際のSlack Webhook URLを設定してテストを実行します

echo "=== Slack通知機能テスト ==="
echo ""

# Webhook URLの確認
if [ -z "$SLACK_WEBHOOK_URL" ]; then
    echo "❌ SLACK_WEBHOOK_URL環境変数が設定されていません"
    echo ""
    echo "テストを実行するには、以下のようにWebhook URLを設定してください："
    echo "export SLACK_WEBHOOK_URL='https://hooks.slack.com/services/T00000000/B00000000/XXXXXXXXXXXXXXXXXXXXXXXX'"
    echo ""
    echo "設定後、このスクリプトを再実行してください。"
    exit 1
fi

echo "✅ SLACK_WEBHOOK_URL が設定されています"
echo "Webhook URL: ${SLACK_WEBHOOK_URL:0:50}..."
echo ""

# アプリケーションのコンパイルとテスト
echo "🔨 アプリケーションをコンパイル中..."
sbt compile

if [ $? -ne 0 ]; then
    echo "❌ コンパイルに失敗しました"
    exit 1
fi

echo "✅ コンパイル成功"
echo ""

# 単体テストの実行
echo "🧪 SlackNotificationService の単体テストを実行中..."
sbt "testOnly services.SlackNotificationServiceTest"

if [ $? -ne 0 ]; then
    echo "❌ 単体テストに失敗しました"
    exit 1
fi

echo "✅ 単体テスト成功"
echo ""

# テスト用のWebhook送信（curlを使用）
echo "📤 テスト通知をSlackに送信中..."
curl -X POST -H 'Content-type: application/json' \
     --data '{"text":"🧪 Slack通知機能テスト\nこのメッセージはcoworkingSpacePortalアプリケーションのSlack通知機能のテストです。"}' \
     "$SLACK_WEBHOOK_URL"

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ テスト通知の送信が完了しました"
    echo "Slackチャンネルでメッセージを確認してください。"
else
    echo ""
    echo "❌ テスト通知の送信に失敗しました"
    exit 1
fi

echo ""
echo "=== テスト完了 ==="
echo ""
echo "🎉 Slack通知機能は正常に動作します！"
echo ""
echo "実際の使用方法："
echo "1. SLACK_WEBHOOK_URL環境変数を設定"
echo "2. アプリケーションを起動: sbt run"
echo "3. ブラウザでアプリケーションにアクセス: http://localhost:9000"
echo "4. ユーザー登録、ログイン、機材操作等を行う"
echo "5. Slackチャンネルで通知を確認"