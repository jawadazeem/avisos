from datetime import datetime, timedelta
import numpy as np
import pandas as pd

# TODO: Make data match real API data.
num_records = 100
start_time = datetime.now()

np.random.seed(42)

timestamps = [
    (start_time - timedelta(minutes=15 * i)).replace(microsecond=0).isoformat().replace("+00:00", "Z")
    for i in range(num_records)
]
total_nodes = np.random.randint(
    low=100, high=1500, size=num_records
)
responsive_ratio = np.random.uniform(
    low=0.85, high=1.00, size=num_records
)
battery_ratio = np.random.uniform(
    low=0.40, high=0.95, size=num_records
)
avg_seconds = np.random.uniform(
    low=5.0, high=300.0, size=num_records
)

flattened_data = {
    "timestamp": timestamps,
    "totalNodesEvaluated": total_nodes,
    "responsiveRatio": np.round(responsive_ratio, 4),
    "batteryAbove50Ratio": np.round(battery_ratio, 4),
    "avgSecondsSinceLastSeen": np.round(avg_seconds, 2),
}

df = pd.DataFrame(flattened_data)
df.to_csv("fleet_training_data.csv", index=False)

print("Dataset successfully generated! Preview:")
print(df.head(3))
