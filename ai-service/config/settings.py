from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """Application settings loaded from environment variables and .env file."""

    OPENAI_API_KEY: str = ""
    AI_MODEL: str = "gpt-3.5-turbo"
    FORECAST_DEFAULT_DAYS: int = 30
    ANOMALY_ZSCORE_THRESHOLD: float = 2.5
    LOG_LEVEL: str = "INFO"

    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"


settings = Settings()
