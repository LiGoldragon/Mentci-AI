# Skill: Mentci STT (Voice Intent Transcription)

This skill allows the agent to transcribe voice recordings (specifically `.opus` files) into high-fidelity text using the specialized vocabulary of the Mentci-AI project.

## Tool: `transcribe_voice_note`
Transcribes a voice recording into text and saves it to the local project transcripts.

### Usage
Run the following command:
```bash
mentci-stt --file <path_to_audio_file>
```

### Context
Use this tool whenever a new voice recording is added to `.voice-recordings/` or when the user references a specific solar-dated audio file. The transcription will include emotional emphasis and specialized project jargon (Aski, Criome, SEMA, etc.).
