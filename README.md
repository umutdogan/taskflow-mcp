# TaskFlow MCP Server

A small in-memory task manager exposed as a [Model Context Protocol](https://modelcontextprotocol.io/) (MCP) server, built with Java, Spring Boot, and [Spring AI](https://docs.spring.io/spring-ai/reference/)'s MCP annotations.

This is the companion project for [*Model Context Protocol (MCP) Explained: Theory, Architecture, and a Java + Spring Boot + Docker Tutorial*](https://umutdogan.com/posts/2026-09-05-model-context-protocol-java-tutorial/) on [umutdogan.com](https://umutdogan.com). The blog post walks through MCP's architecture and primitives, then builds this exact server step by step — every command and JSON response in the post was captured from an actual run of this code.

## What it exposes

- **Tool** `add_task` — add a task with a title and an optional priority (`LOW`, `MEDIUM`, `HIGH`)
- **Tool** `list_tasks` — list tasks, optionally filtered by status (`open`, `done`, `all`)
- **Tool** `complete_task` — mark a task done by id
- **Resource** `tasks://all` — a JSON snapshot of every task

Transport is **Streamable HTTP** (`spring-ai-starter-mcp-server-webmvc`), served at `POST /mcp`.

## Running it

Requires Java 21+ and Maven.

```bash
mvn clean package
java -jar target/taskflow-mcp.jar
```

The server starts on port `8080`.

### Talking to it with curl

```bash
# 1. Handshake — note the Mcp-Session-Id in the response headers
curl -i -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -H "Accept: application/json, text/event-stream" \
  -d '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-06-18","capabilities":{},"clientInfo":{"name":"curl-client","version":"1.0.0"}}}'

# 2. Confirm the handshake (use the session id from step 1)
SESSION="<paste Mcp-Session-Id here>"
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" -H "Accept: application/json, text/event-stream" \
  -H "Mcp-Session-Id: $SESSION" \
  -d '{"jsonrpc":"2.0","method":"notifications/initialized"}'

# 3. List the tools
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" -H "Accept: application/json, text/event-stream" \
  -H "Mcp-Session-Id: $SESSION" \
  -d '{"jsonrpc":"2.0","id":2,"method":"tools/list"}'

# 4. Call one
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" -H "Accept: application/json, text/event-stream" \
  -H "Mcp-Session-Id: $SESSION" \
  -d '{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"add_task","arguments":{"title":"Write MCP blog post","priority":"high"}}}'
```

Or skip curl entirely and connect with the official [MCP Inspector](https://github.com/modelcontextprotocol/inspector):

```bash
npx @modelcontextprotocol/inspector
```

## Running it with Docker

```bash
docker build -t taskflow-mcp .
docker run --rm -p 8080:8080 taskflow-mcp
```

Same server, same API — the multi-stage `Dockerfile` builds with Maven and runs on a slim JRE image as a non-root user.

## Project structure

```
src/main/java/com/umutdogan/ai/taskflow/
├── Task.java                  # the domain record
├── TaskService.java           # in-memory storage, no MCP awareness
├── TaskTools.java             # @McpTool / @McpResource — the only MCP-specific code
└── TaskflowMcpApplication.java
```

The MCP layer is intentionally a thin adapter over a plain Spring service — see the blog post for why that separation matters.

## License

MIT — see [LICENSE](LICENSE).
