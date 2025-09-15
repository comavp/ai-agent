# План исправления ошибки tool calling в deepseek-ai-agent

## Анализ проблемы

### Описание ошибки
```
400: An assistant message with 'tool_calls' must be followed by tool messages responding to each 'tool_call_id'. (insufficient tool messages following tool_calls message)
```

### Причины проблемы

1. **Неправильное формирование tool message в диалоге** (Agent.java:68-76):
   - При создании ChatCompletionMessage с ролью "tool" неправильно передается поле `toolCalls`
   - Согласно OpenAI API, tool message должно содержать только `toolCallId`, а не весь массив `toolCalls`

2. **Ошибка в логике получения toolCallId** (Agent.java:105):
   - В методе `sendUserMessage` при обработке tool message используется `item.toolCalls().get().get(0).asFunction().id()`
   - Но tool message не должно содержать toolCalls - оно должно содержать только toolCallId

3. **Проблема в структуре диалога**:
   - После выполнения инструмента в диалог добавляется сообщение с некорректной структурой
   - Tool message должно ссылаться на конкретный `tool_call_id` из предыдущего assistant message

## Решение

### Изменения в Agent.java

1. **Исправить создание tool message** (строки 68-76):
   - Удалить поле `toolCalls` из tool message
   - Добавить поле `toolCallId` с правильным ID из assistant message
   - Сохранить ID вызова инструмента для использования в tool message

2. **Исправить обработку tool message в sendUserMessage** (строки 102-106):
   - Убрать логику получения toolCallId из самого tool message
   - Использовать сохраненный toolCallId

3. **Добавить сохранение toolCallId**:
   - При выполнении инструмента сохранять ID вызова для последующего использования

### Конкретные изменения кода

1. В методе `run()` - исправить создание tool message:
   ```java
   // Сохранить toolCallId перед выполнением
   String toolCallId = assistantMessage.toolCalls().get().get(0).asFunction().id();

   // Создать tool message без toolCalls, но с toolCallId
   dialog.add(ChatCompletionMessage.builder()
           .role(JsonValue.from("tool"))
           .content(mapper.writeValueAsString(result))
           .refusal("")
           // Убрать .toolCalls(assistantMessage.toolCalls().get())
           .build());
   ```

2. В методе `sendUserMessage()` - исправить обработку tool message:
   ```java
   case "tool" ->
       ChatCompletionMessageParam.ofTool(ChatCompletionToolMessageParam.builder()
               .content(item.content().get())
               .toolCallId(сохраненный_toolCallId) // Использовать сохраненный ID
               .build());
   ```

### Альтернативное решение

Переработать структуру данных:
- Создать отдельный класс для хранения контекста диалога
- Сохранять связку между tool messages и их toolCallId
- Упростить логику обработки различных типов сообщений

## Ожидаемый результат

После исправления:
1. Tool calling будет работать корректно при повторных вызовах
2. Диалог будет правильно формироваться согласно OpenAI API
3. Ошибка "insufficient tool messages following tool_calls message" исчезнет