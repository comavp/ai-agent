# План исправления ошибки tool calling в deepseek-ai-agent (Plan 2)

## Анализ новой проблемы

### Описание новой ошибки
```
400: An assistant message with 'tool_calls' must be followed by tool messages responding to each 'tool_call_id', The following tool_call_ids did not have response messages: call_00_uKVLhXo70UFDzegv0U0A4Cgl
```

### Контекст
После исправления в коммите 220b98b3, первоначальная ошибка "insufficient tool messages following tool_calls message" была решена, но появилась новая проблема - конкретный `tool_call_id` не получает ответного сообщения.

### Анализ корневой причины

#### Проблема с управлением `lastToolCallId`
1. **Единственная переменная для хранения ID** (Agent.java:31):
   - `private String lastToolCallId;` - используется для хранения ID последнего вызова инструмента
   - Проблема: если есть несколько tool calls или асинхронные операции, переменная может быть перезаписана

2. **Неправильная логика присвоения и использования** (Agent.java:67, 106):
   - Строка 67: `lastToolCallId = assistantMessage.toolCalls().get().get(0).asFunction().id();`
   - Строка 106: `.toolCallId(lastToolCallId)`
   - Проблема: между присвоением и использованием могут происходить другие операции

3. **Отсутствие связи между tool message и соответствующим tool call**:
   - Tool message создается через несколько строк после получения tool call
   - Нет гарантии, что `lastToolCallId` не изменился к моменту создания tool message

#### Сценарий возникновения ошибки
1. DeepSeek отправляет assistant message с tool_call (ID: `call_00_uKVLhXo70UFDzegv0U0A4Cgl`)
2. Агент сохраняет ID в `lastToolCallId`
3. Инструмент выполняется
4. При создании tool message используется `lastToolCallId`, но по какой-то причине ID не соответствует ожидаемому
5. OpenAI API получает tool message с неправильным `tool_call_id`

## Решение

### Основные изменения

1. **Немедленное связывание tool call с tool message**:
   - Сохранять `toolCallId` в локальной переменной в том же блоке, где обрабатывается tool call
   - Использовать эту локальную переменную при создании tool message

2. **Убрать поле `lastToolCallId`**:
   - Поскольку связывание должно происходить немедленно, нет необходимости в поле класса

### Конкретные изменения кода

#### В методе `run()` (строки 65-78):
```java
} else if (assistantMessage.toolCalls().isPresent()) {
    readUserInput = false;

    // Сохранить toolCallId локально для немедленного использования
    String toolCallId = assistantMessage.toolCalls().get().get(0).asFunction().id();

    ToolResult result = executeTool(assistantMessage.toolCalls().get().get(0).asFunction().function());
    try {
        dialog.add(ChatCompletionMessage.builder()
                .role(JsonValue.from("tool"))
                .content(mapper.writeValueAsString(result))
                .refusal("")
                // Использовать локальную переменную вместо поля класса
                .toolCallId(toolCallId)  // Добавить поле toolCallId в ChatCompletionMessage
                .build());
    } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
    }
}
```

#### В методе `sendUserMessage()` (строки 103-107):
```java
case "tool" ->
        ChatCompletionMessageParam.ofTool(ChatCompletionToolMessageParam.builder()
                .content(item.content().get())
                .toolCallId(item.toolCallId())  // Использовать toolCallId из сообщения
                .build());
```

### Проблема с архитектурой

**Основная архитектурная проблема**: `ChatCompletionMessage` (объект из OpenAI библиотеки) не имеет поля `toolCallId` для tool messages, но `ChatCompletionToolMessageParam` (параметр для API) требует его.

### Альтернативные решения

#### Решение 1: Создать wrapper класс для сообщений
```java
public class DialogMessage {
    private ChatCompletionMessage message;
    private String toolCallId; // Для tool messages

    // Конструкторы и геттеры
}
```

#### Решение 2: Использовать Map для связи сообщений с tool call IDs
```java
private Map<Integer, String> toolCallIds = new HashMap<>(); // index -> toolCallId
```

#### Решение 3: Реструктуризация обработки (рекомендуемое)
Обрабатывать создание tool message параметров непосредственно при отправке, используя информацию из dialog и контекста.

## Рекомендуемая реализация

1. Убрать поле `lastToolCallId` из класса
2. При обработке tool call сохранять связку между позицией в dialog и toolCallId
3. При создании параметров для API использовать эту связку для получения правильного toolCallId

Это решение обеспечит правильную связь между tool calls и tool messages, исключив возможность использования неправильного toolCallId.