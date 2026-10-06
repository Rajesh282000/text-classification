# AI-Assisted Text Classification API

Java + Spring Boot backend assignment.

## API

POST `/api/classify`

Request:
```json
{
  "text": "I am very unhappy with the service."
}
```

Response:
```json
{
  "category": "Complaint",
  "confidence": 0.95
}
```

## Environment variables

```text
OPENAI_API_KEY=your_api_key
OPENAI_MODEL=gpt-5
```

Never commit the API key to GitHub.

## AI integration

The backend sends the input text to the OpenAI Responses API with a classification prompt. The model is instructed to return one of Complaint, Query, Feedback, or Other together with a confidence value.
