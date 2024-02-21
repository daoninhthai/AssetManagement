"""Utility functions for data processing and transformation."""

from datetime import datetime, timedelta
from typing import List

import numpy as np
import pandas as pd


def parse_date(date_str: str) -> datetime:
    """Parse an ISO-format date string into a datetime object.

    Supports formats: YYYY-MM-DD, YYYY/MM/DD.
    """
    for fmt in ("%Y-%m-%d", "%Y/%m/%d"):
        try:
            return datetime.strptime(date_str, fmt)
        except ValueError:
            continue
    raise ValueError(f"Unable to parse date string: {date_str}")


def create_date_range(start: datetime, end: datetime) -> List[datetime]:
    """Return a list of datetime objects from *start* to *end* inclusive."""
    if end < start:
        return []
    num_days = (end - start).days + 1
    return [start + timedelta(days=i) for i in range(num_days)]


def fill_missing_dates(
    df: pd.DataFrame,
    date_col: str = "date",
    fill_value: int = 0,
) -> pd.DataFrame:
    """Ensure the DataFrame contains a row for every date in its range.

    Missing dates are filled with *fill_value* for all numeric columns.
    """
    if df.empty:
        return df

    df = df.copy()
    df[date_col] = pd.to_datetime(df[date_col])
    df = df.sort_values(date_col).reset_index(drop=True)

    full_range = pd.date_range(start=df[date_col].min(), end=df[date_col].max())
    df = df.set_index(date_col).reindex(full_range).rename_axis(date_col).reset_index()

    numeric_cols = df.select_dtypes(include=[np.number]).columns
    df[numeric_cols] = df[numeric_cols].fillna(fill_value)

    return df


def calculate_moving_average(series: pd.Series, window: int = 7) -> pd.Series:
    """Compute the rolling mean of *series* with the given *window* size.

    Leading NaN values are back-filled with the first available average.
    """
    ma = series.rolling(window=window, min_periods=1).mean()
    return ma


def normalize_data(series: pd.Series) -> pd.Series:
    """Min-max normalise a pandas Series to the [0, 1] range.

    Returns a zero series if min == max (constant data).
    """
    min_val = series.min()
    max_val = series.max()
    if max_val == min_val:
        return pd.Series(np.zeros(len(series)), index=series.index)
    return (series - min_val) / (max_val - min_val)
