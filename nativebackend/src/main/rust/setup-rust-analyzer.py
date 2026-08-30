import json
import subprocess
import os
from pathlib import Path

if Path(".vscode").joinpath("settings.json").exists():
  print("It seems like you've already run this script or you have your own .vscode/settings.json")
  exit(0)

result = subprocess.run(["cargo", "ndk-env", "-P", "23", "--target", "arm64-v8a", "--json"], capture_output=True, text=True)
result.check_returncode()

env_vars = json.loads(result.stdout)

rust_analyzer_settings = {
  "rust-analyzer.cargo.target": "aarch64-linux-android",
  "rust-analyzer.check.targets": [
    "aarch64-linux-android"
  ],
  "rust-analyzer.server.extraEnv": env_vars
}

script_dir = os.path.dirname(os.path.realpath(__file__))
Path(script_dir).joinpath(".vscode").mkdir(exist_ok=True)

file = open(os.path.join(script_dir, ".vscode", "settings.json"), "w")
file.write(json.dumps(rust_analyzer_settings))