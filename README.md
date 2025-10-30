# QuoteReply Bot

A Telegram bot that quotes and replies to text messages.

## Configuration

The bot uses a `.env` file for configuration. Copy `.env.example` to `.env` and configure your settings:

```bash
cp .env.example .env
```

### Required Configuration

- `TELEGRAM_BOT_TOKEN`: Your Telegram bot token from [@BotFather](https://t.me/BotFather)

### Optional Configuration

- `TELEGRAM_BOT_PROXY_ADDRESS`: Proxy server address (optional)
  - Supports Socks5 proxy: `socks5://127.0.0.1:7890`
  - Supports HTTP proxy: `http://127.0.0.1:8080`
  - Supports HTTPS proxy: `https://127.0.0.1:8080`

## Running the Bot

```bash
./gradlew run
```

## Building

```bash
./gradlew build
```
