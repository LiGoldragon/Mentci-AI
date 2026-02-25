import capnp
import os
import sys

# Try to load the schema
try:
    mentci_capnp = capnp.load("Components/schema/mentci.capnp")
except Exception as e:
    print(f"Error loading capnp: {e}")
    sys.exit(1)

req = mentci_capnp.SttRequest.new_message()
req.audioPath = ".voice-recordings/12.7.56.opus"
req.model = "gemini-2.5-pro"
vocab = req.init("vocabulary", 2)
vocab[0] = "Raddii"
vocab[1] = "Aski"

with open("test_req.capnp", "wb") as f:
    req.write_packed(f)

print("Wrote test_req.capnp")
