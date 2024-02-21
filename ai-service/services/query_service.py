"""Natural-language inventory query service using LangChain / OpenAI."""

import logging
from typing import List

from config.settings import settings
from models.schemas import InventoryContext, QueryRequest, QueryResponse

logger = logging.getLogger(__name__)

# ── Keyword mappings for fallback mode ────────────────────────────────────────
_LOW_STOCK_KEYWORDS = {"sắp hết hàng", "hết hàng", "low stock", "thiếu hàng", "cạn kho"}
_TOP_KEYWORDS = {"top", "bán chạy", "best seller", "nhiều nhất", "phổ biến"}
_WAREHOUSE_KEYWORDS = {"kho", "warehouse", "nhà kho", "kho hàng"}
_SUMMARY_KEYWORDS = {"tổng quan", "summary", "tóm tắt", "overview", "báo cáo"}


def process_query(request: QueryRequest) -> QueryResponse:
    """Answer a natural-language inventory question.

    When an OpenAI API key is configured the service delegates to LangChain's
    ChatOpenAI model.  Otherwise a lightweight keyword-based fallback produces
    a best-effort answer from the supplied inventory context.
    """
    question = request.question.strip()
    language = request.language
    context = request.inventory_context

    # Build a structured context string
    context_text = _build_context_text(context) if context else ""

    # ── LangChain / OpenAI path ──────────────────────────────────────────
    if settings.OPENAI_API_KEY:
        try:
            return _query_with_llm(question, context_text, language, context)
        except Exception as exc:
            logger.warning("LLM query failed, falling back to rules: %s", exc)

    # ── Fallback (keyword) path ──────────────────────────────────────────
    return _query_with_fallback(question, language, context)


# ─── LLM-based query ─────────────────────────────────────────────────────────


def _query_with_llm(
    question: str,
    context_text: str,
    language: str,
    context: InventoryContext | None,
) -> QueryResponse:
    """Use LangChain ChatOpenAI to answer the question."""
    from langchain_openai import ChatOpenAI
    from langchain.schema import HumanMessage, SystemMessage

    lang_name = "tiếng Việt" if language == "vi" else language
    system_prompt = (
        "You are an AI assistant for warehouse inventory management. "
        "Answer questions based on the provided inventory data. "
        f"Respond in {lang_name}. Be concise and data-driven."
    )

    human_content = f"Inventory context:\n{context_text}\n\nQuestion: {question}"

    llm = ChatOpenAI(
        model=settings.AI_MODEL,
        openai_api_key=settings.OPENAI_API_KEY,
        temperature=0.3,
        max_tokens=1024,
    )

    messages = [
        SystemMessage(content=system_prompt),
        HumanMessage(content=human_content),
    ]

    response = llm.invoke(messages)
    answer = response.content.strip()

    return QueryResponse(
        question=question,
        answer=answer,
        confidence=0.85,
        sources=["openai", "inventory_context"],
        language=language,
    )


# ─── Keyword fallback ────────────────────────────────────────────────────────


def _query_with_fallback(
    question: str,
    language: str,
    context: InventoryContext | None,
) -> QueryResponse:
    """Pattern-match the question to common intents and build an answer."""
    q_lower = question.lower()

    answer: str
    sources: List[str] = ["rule_based"]
    confidence = 0.6

    if _matches_any(q_lower, _LOW_STOCK_KEYWORDS):
        answer = _answer_low_stock(context, language)
        sources.append("low_stock_data")
    elif _matches_any(q_lower, _TOP_KEYWORDS):
        answer = _answer_top_products(context, language)
        sources.append("category_data")
    elif _matches_any(q_lower, _WAREHOUSE_KEYWORDS):
        answer = _answer_warehouse(context, language)
        sources.append("warehouse_data")
    elif _matches_any(q_lower, _SUMMARY_KEYWORDS):
        answer = _answer_summary(context, language)
        sources.append("summary_data")
        confidence = 0.7
    else:
        answer = _answer_generic(context, language)
        confidence = 0.4

    return QueryResponse(
        question=question,
        answer=answer,
        confidence=confidence,
        sources=sources,
        language=language,
    )


# ─── Answer builders ─────────────────────────────────────────────────────────


def _answer_low_stock(ctx: InventoryContext | None, lang: str) -> str:
    if not ctx or not ctx.low_stock_products:
        if lang == "vi":
            return "Hiện không có dữ liệu về sản phẩm sắp hết hàng."
        return "No low-stock product data is currently available."

    items = ctx.low_stock_products[:10]
    if lang == "vi":
        lines = ["Danh sách sản phẩm sắp hết hàng:"]
        for i, p in enumerate(items, 1):
            name = p.get("name", p.get("product_name", "N/A"))
            stock = p.get("stock", p.get("current_stock", "N/A"))
            lines.append(f"  {i}. {name} — tồn kho: {stock}")
        return "\n".join(lines)

    lines = ["Low-stock products:"]
    for i, p in enumerate(items, 1):
        name = p.get("name", p.get("product_name", "N/A"))
        stock = p.get("stock", p.get("current_stock", "N/A"))
        lines.append(f"  {i}. {name} — stock: {stock}")
    return "\n".join(lines)


def _answer_top_products(ctx: InventoryContext | None, lang: str) -> str:
    if not ctx or not ctx.top_categories:
        if lang == "vi":
            return "Hiện không có dữ liệu về danh mục sản phẩm hàng đầu."
        return "No top-category data is currently available."

    cats = ctx.top_categories[:5]
    if lang == "vi":
        lines = ["Danh mục sản phẩm hàng đầu:"]
        for i, c in enumerate(cats, 1):
            name = c.get("name", c.get("category_name", "N/A"))
            count = c.get("count", c.get("product_count", "N/A"))
            lines.append(f"  {i}. {name} — số sản phẩm: {count}")
        return "\n".join(lines)

    lines = ["Top categories:"]
    for i, c in enumerate(cats, 1):
        name = c.get("name", c.get("category_name", "N/A"))
        count = c.get("count", c.get("product_count", "N/A"))
        lines.append(f"  {i}. {name} — products: {count}")
    return "\n".join(lines)


def _answer_warehouse(ctx: InventoryContext | None, lang: str) -> str:
    if not ctx:
        if lang == "vi":
            return "Hiện không có dữ liệu về kho hàng."
        return "No warehouse data is currently available."

    if lang == "vi":
        return (
            f"Hệ thống hiện có {ctx.total_warehouses} kho hàng "
            f"với tổng cộng {ctx.total_products} sản phẩm."
        )
    return (
        f"The system currently has {ctx.total_warehouses} warehouses "
        f"with a total of {ctx.total_products} products."
    )


def _answer_summary(ctx: InventoryContext | None, lang: str) -> str:
    if not ctx:
        if lang == "vi":
            return "Hiện không có dữ liệu tổng quan."
        return "No summary data is currently available."

    low_count = len(ctx.low_stock_products)
    recent_count = len(ctx.recent_movements)

    if lang == "vi":
        return (
            f"Tổng quan kho hàng:\n"
            f"  - Tổng sản phẩm: {ctx.total_products}\n"
            f"  - Tổng kho: {ctx.total_warehouses}\n"
            f"  - Sản phẩm sắp hết: {low_count}\n"
            f"  - Giao dịch gần đây: {recent_count}"
        )
    return (
        f"Inventory overview:\n"
        f"  - Total products: {ctx.total_products}\n"
        f"  - Total warehouses: {ctx.total_warehouses}\n"
        f"  - Low-stock items: {low_count}\n"
        f"  - Recent movements: {recent_count}"
    )


def _answer_generic(ctx: InventoryContext | None, lang: str) -> str:
    if ctx:
        return _answer_summary(ctx, lang)
    if lang == "vi":
        return (
            "Xin lỗi, tôi không có đủ dữ liệu để trả lời câu hỏi này. "
            "Vui lòng cung cấp thêm ngữ cảnh về kho hàng."
        )
    return (
        "Sorry, I don't have enough data to answer this question. "
        "Please provide more inventory context."
    )


# ─── Utilities ────────────────────────────────────────────────────────────────


def _matches_any(text: str, keywords: set) -> bool:
    return any(kw in text for kw in keywords)


def _build_context_text(ctx: InventoryContext) -> str:
    """Serialise an InventoryContext into a structured string for the LLM."""
    lines = [
        f"Total products: {ctx.total_products}",
        f"Total warehouses: {ctx.total_warehouses}",
    ]

    if ctx.low_stock_products:
        lines.append("Low-stock products:")
        for p in ctx.low_stock_products[:15]:
            name = p.get("name", p.get("product_name", "N/A"))
            stock = p.get("stock", p.get("current_stock", "N/A"))
            lines.append(f"  - {name}: {stock} units")

    if ctx.recent_movements:
        lines.append("Recent movements (last entries):")
        for m in ctx.recent_movements[:10]:
            lines.append(f"  - {m}")

    if ctx.top_categories:
        lines.append("Top categories:")
        for c in ctx.top_categories[:5]:
            name = c.get("name", c.get("category_name", "N/A"))
            count = c.get("count", c.get("product_count", "N/A"))
            lines.append(f"  - {name}: {count}")

    return "\n".join(lines)
