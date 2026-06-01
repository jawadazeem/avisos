import { useEffect, useState } from "react";
import { useWebSocket } from "../context/WebSocketContext";

export function useSubscription<T>(topic: string): T | null {
  const { client, connected } = useWebSocket();
  const [message, setMessage] = useState<T | null>(null);

  useEffect(() => {
    if (!client || !connected) return;

    const subscription = client.subscribe(topic, (msg) => {
      try {
        setMessage(JSON.parse(msg.body) as T);
      } catch {
        console.error("Failed to parse message from", topic);
      }
    });

    return () => {
      subscription.unsubscribe();
    };
  }, [client, connected, topic]);

  return message;
}
