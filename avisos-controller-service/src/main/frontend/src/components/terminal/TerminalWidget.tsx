import { useEffect, useRef, useCallback } from "react";
import { Terminal } from "@xterm/xterm";
import { FitAddon } from "@xterm/addon-fit";
import { useWebSocket } from "../../context/WebSocketContext";
import "@xterm/xterm/css/xterm.css";
import "./TerminalWidget.css";

const PROMPT = "\x1b[32mavisos>\x1b[0m ";

export function TerminalWidget() {
  const termRef = useRef<HTMLDivElement>(null);
  const xtermRef = useRef<Terminal | null>(null);
  const fitRef = useRef<FitAddon | null>(null);
  const inputRef = useRef("");
  const { client, connected } = useWebSocket();

  const writePrompt = useCallback(() => {
    xtermRef.current?.write("\r\n" + PROMPT);
  }, []);

  useEffect(() => {
    if (!termRef.current) return;

    const term = new Terminal({
      theme: {
        background: "#0a0e14",
        foreground: "#00ff88",
        cursor: "#00ff88",
        cursorAccent: "#0a0e14",
        selectionBackground: "#1a2332",
      },
      fontFamily: "'JetBrains Mono', 'Fira Code', monospace",
      fontSize: 13,
      cursorBlink: true,
      cursorStyle: "block",
    });

    const fit = new FitAddon();
    term.loadAddon(fit);
    term.open(termRef.current);
    fit.fit();

    xtermRef.current = term;
    fitRef.current = fit;

    term.writeln(
      "\x1b[36m  ___  _   _____ ___  ___  ___\x1b[0m",
    );
    term.writeln(
      "\x1b[36m / _ \\| | / /_ _/ __|/ _ \\/ __|\x1b[0m",
    );
    term.writeln(
      "\x1b[36m/ /_\\ \\ |/ / | |\\__ \\ (_) \\__ \\\x1b[0m",
    );
    term.writeln(
      "\x1b[36m\\_/ \\_/___/ |___|___/\\___/|___/\x1b[0m",
    );
    term.writeln("");
    term.writeln(
      "\x1b[33mAdvanced Visual Infrastructure Secure Operational Systems\x1b[0m",
    );
    term.writeln("\x1b[90mType 'help' for available commands.\x1b[0m");
    term.write(PROMPT);

    term.onData((data: string) => {
      if (data === "\r") {
        const cmd = inputRef.current.trim();
        inputRef.current = "";
        if (cmd.length === 0) {
          writePrompt();
          return;
        }
        term.write("\r\n");
        if (cmd === "clear") {
          term.clear();
          term.write(PROMPT);
          return;
        }
        sendCommand(cmd);
      } else if (data === "\x7f") {
        if (inputRef.current.length > 0) {
          inputRef.current = inputRef.current.slice(0, -1);
          term.write("\b \b");
        }
      } else if (data >= " ") {
        inputRef.current += data;
        term.write(data);
      }
    });

    const onResize = () => fit.fit();
    window.addEventListener("resize", onResize);

    const onQuickCmd = (e: Event) => {
      const cmd = (e as CustomEvent<string>).detail;
      inputRef.current = "";
      term.write("\r\n");
      term.write(PROMPT + cmd);
      term.write("\r\n");
      sendCommand(cmd);
    };
    window.addEventListener("cli-quick-command", onQuickCmd);

    return () => {
      window.removeEventListener("resize", onResize);
      window.removeEventListener("cli-quick-command", onQuickCmd);
      term.dispose();
    };
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    if (!client || !connected) return;

    const sub = client.subscribe("/topic/cli", (msg) => {
      const term = xtermRef.current;
      if (!term) return;

      try {
        const resp = JSON.parse(msg.body);
        const lines = (resp.output as string).split("\n");
        for (const line of lines) {
          if (line.length > 0) {
            term.writeln(line);
          }
        }
        term.writeln(
          `\x1b[90m[${resp.executionMs}ms]\x1b[0m`,
        );
      } catch {
        term.writeln("\x1b[31mError parsing response\x1b[0m");
      }
      writePrompt();
    });

    return () => sub.unsubscribe();
  }, [client, connected, writePrompt]);

  function sendCommand(cmd: string) {
    if (!client || !connected) {
      xtermRef.current?.writeln(
        "\x1b[31mNot connected to server\x1b[0m",
      );
      writePrompt();
      return;
    }
    client.publish({
      destination: "/app/cli",
      body: JSON.stringify({ command: cmd }),
    });
  }

  return <div ref={termRef} className="terminal-container" />;
}
