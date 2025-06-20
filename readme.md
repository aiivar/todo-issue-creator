# TODO Issue Creator

Инструмент для автоматического создания GitHub issues из TODO-комментариев в коде. Приложение сканирует ваш код на наличие TODO-комментариев и создает соответствующие issues в GitHub репозитории.

## Необходимые компоненты

- Java 21 или выше
- Maven 3.6+ (или используйте встроенный Maven wrapper)
- GitHub аккаунт и Personal Access Token (PAT)

## Быстрый старт

1. **Клонирование репозитория:**
   ```bash
   git clone <url-репозитория>
   cd todo-issue-creator
   ```

2. **Настройка конфигурации:**
   
   В файле `src/main/resources/.todo-to-issue.yaml` настройте следующие параметры:
   ```yaml
   github:
     repo: "ваш-логин/ваш-репозиторий"  # Например: "user/my-project"
     token: "ваш-github-token"           # Вставьте сюда ваш GitHub токен

   defaults:
     priority: "medium"
     category: "feature"

   filters:
     include:
       - "*.java"
       - "*.kt"
       - "*.py"
       - "*.js"
       - "*.ts"
     exclude:
       - "node_modules/**"
       - "target/**"
       - "build/**"
       - "dist/**"
       - "**/*Test.java"

   issueTemplate:
     title: "{description}"
     body: |
       **Задача**: {description}
       **Файл**: {file}
       **Строка**: {line}
       **Приоритет**: {priority}
       **Категория**: {category}
       {assignee}
       {tags}
     labels:
       - "good first issue"
       - "{category}"
       - "{priority}"
   ```

   Основные параметры для изменения:
   - `github.repo`: укажите ваш репозиторий в формате "пользователь/репозиторий"
   - `github.token`: вставьте ваш GitHub токен
   - При необходимости настройте фильтры файлов в секции `filters`

3. **Сборка проекта:**
   
   Windows:
   ```bash
   mvnw.cmd clean install
   ```
   
   Linux/MacOS:
   ```bash
   ./mvnw clean install
   ```

4. **Запуск приложения:**

   Тестовый режим (без создания issues):
   
   Windows:
   ```bash
   mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--dry-run --verbose"
   ```
   
   Linux/MacOS:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.arguments="--dry-run --verbose"
   ```

   Рабочий режим (с созданием issues):
   
   Windows:
   ```bash
   mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--verbose"
   ```
   
   Linux/MacOS:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.arguments="--verbose"
   ```

## Получение GitHub токена

1. Перейдите в настройки GitHub: Settings -> Developer settings -> Personal access tokens -> Tokens (classic)
2. Нажмите "Generate new token"
3. Выберите scope `repo`
4. Скопируйте токен и вставьте его в поле `github.token` в файле конфигурации

## Формат TODO-комментариев

1. Простой формат:
   ```java
   //todo: Добавить валидацию формы
   ```

2. Расширенный формат:
   ```java
   //todo: Добавить валидацию формы | priority: high | category: feature | assignee: @username | tags: frontend,validation
   ```

## Параметры запуска

- `--dry-run`: тестовый режим без создания issues
- `--verbose`: подробный вывод информации о работе приложения

## Зависимости проекта

- Spring Boot 3.2.3
- GitHub API 1.319
- JavaParser 3.25.9
- SnakeYAML 2.2
- JGit 6.9.0
- Lombok

## Техническое задание

# Техническое задание (ТЗ) для инструмента автоматического создания Issue из //todo комментариев на GitHub

## 1. Общее описание

Инструмент предназначен для автоматизации создания задач типа Issue в репозиториях GitHub на основе комментариев `//todo` в исходном коде. Он сканирует код, извлекает `//todo` комментарии, формирует описание задачи и публикует её как issue в указанном репозитории GitHub.

## 2. Цели и задачи

### Цели:

- Упростить процесс создания задач для новых контрибьюторов.
- Автоматизировать документирование задач из комментариев.
- Обеспечить прозрачность и структурированность задач.

### Задачи:

- Сканирование исходного кода для поиска `//todo` комментариев.
- Извлечение метаданных (описание, приоритет, категория, назначенный пользователь).
- Формирование структурированного описания задачи.
- Интеграция с GitHub API для создания issue.
- Обеспечение гибкой настройки формата комментариев и шаблонов issue.

## 3. Функциональные требования

### 3.1. Сканирование кода

- **Поддерживаемые языки**: JavaScript, TypeScript, Python, Java, C++, C#, Ruby, PHP и др. (расширяемый список через конфигурацию).
- **Поиск комментариев**:
  - Обнаружение `//todo` или `//TODO` (регистр не важен).
  - Поддержка многострочных комментариев (например, `/* todo */` для C-стилей).
  - Игнорирование нерелевантных комментариев (например, закомментированный код или `//todo` внутри строковых литералов).
- **Фильтрация**:
  - Исключение файлов/папок по `.gitignore` или пользовательским паттернам (например, `node_modules`, `dist`).
  - Поддержка включения/исключения файлов по расширениям (например, только `.js`, `.py`).

### 3.2. Парсинг комментариев

- **Формат комментариев**:
  - Минимальный: `//todo: <описание задачи>`.
  - Расширенный: `//todo: <описание> | priority: <low/medium/high> | category: <bug/feature/refactor> | assignee: <@username> | tags: <tag1,tag2>`.
  - Пример:

    ```javascript
    //todo: Добавить валидацию формы | priority: high | category: feature | assignee: @john_doe | tags: frontend,forms
    ```
- **Обработка метаданных**:
  - Если метаданные отсутствуют, применяются значения по умолчанию (например, `priority: medium`, `category: feature`).
  - Поддержка пользовательских полей через конфигурацию (например, `deadline: 2025-05-01`).
- **Валидация**:
  - Проверка корректности формата (например, валидные значения для `priority`).
  - Логирование некорректных комментариев с указанием файла и строки.

### 3.3. Формирование issue

- **Шаблон issue**:

  ```
  **Заголовок**: <Краткое описание из //todo>
  **Описание**:
  - Задача: <Полное описание задачи>
  - Файл: <Путь к файлу>
  - Строка: <Номер строки>
  - Приоритет: <low/medium/high>
  - Категория: <bug/feature/refactor>
  - Назначено: <@username, если указан>
  - Теги: <tag1, tag2, если указаны>
  **Метки**: good first issue, <category>, <priority>, <tags>
  ```
- **Пример issue**:

  ```
  **Заголовок**: Добавить валидацию формы
  **Описание**:
  - Задача: Добавить валидацию формы
  - Файл: src/components/Form.js
  - Строка: 42
  - Приоритет: high
  - Категория: feature
  - Назначено: @john_doe
  - Теги: frontend, forms
  **Метки**: good first issue, feature, high, frontend, forms
  ```

### 3.4. Интеграция с GitHub

- **Аутентификация**:
  - Поддержка GitHub Personal Access Token или OAuth.
  - Хранение токена в переменных окружения или зашифрованном виде.
- **Создание issue**:
  - Использование GitHub API для публикации issue.
  - Поддержка назначения пользователей (`assignee`) и меток (`labels`).

### 3.5. Режимы работы

- **Обычный режим**: Сканирование, парсинг и создание issue.
- **Dry-run режим**:
  - Вывод списка задач, которые будут созданы, без обращения к GitHub API.
  - Форматированный вывод в консоль (см. раздел UX).
- **Периодический запуск**:
  - Поддержка интеграции с CI/CD (например, запуск при каждом коммите).
  - Фильтрация только новых или изменённых `//todo` комментариев (сравнение с предыдущим состоянием).

### 3.6. Настройки

- **Конфигурационный файл** (`.todo-to-issue.yaml`):
  - Репозиторий GitHub (URL или `username/repository`).
  - Шаблон issue (заголовок, тело, метки).
  - Правила парсинга (регулярные выражения, пользовательские поля).
  - Фильтры файлов (включение/исключение по паттернам).
  - Значения по умолчанию (приоритет, категория).
- **Пример конфигурации**:

  ```yaml
  github:
    repo: "my-org/my-repo"
    token: "env:GITHUB_TOKEN"
  defaults:
    priority: "medium"
    category: "feature"
  filters:
    include: ["*.js", "*.py"]
    exclude: ["node_modules", "dist"]
  issueTemplate:
    title: "{description}"
    body: |
      **Задача**: {description}
      **Файл**: {file}
      **Строка**: {line}
      **Приоритет**: {priority}
      **Категория**: {category}
    labels: ["good first issue", "{category}", "{priority}"]
  ```
- **Командная строка**:
  - Поддержка флагов: `--repo`, `--dry-run`.
  - Пример: `todo-to-issue --repo my-org/my-repo --dry-run`.

## 4. Интерфейсные макеты UX (CLI)

Инструмент использует CLI-интерфейс, поэтому UX фокусируется на читаемости вывода, информативности сообщений и удобстве взаимодействия.

### 4.1. CLI-флаги

- `--dry-run`: Показать задачи, которые будут созданы, без их публикации.
- `--repo <repo>`: Репозиторий GitHub (например, `username/repository`).

### 4.2. Макеты CLI-выводов

#### 4.2.1. Обычный запуск

```
$ todo-to-issue --repo my-org/my-repo
[INFO] Сканирование репозитория...
[INFO] Найдено 3 //todo комментария
[INFO] Создано 2 новых issue в my-org/my-repo:
  - #123: Добавить валидацию формы (src/components/Form.js:42)
  - #124: Исправить стили кнопки (src/styles/Button.css:15)
[WARNING] 1 комментарий пропущен из-за дублирования
[INFO] Завершено за 12.3 секунды
```

#### 4.2.2. Dry-run режим

```
$ todo-to-issue --repo my-org/my-repo --dry-run
[INFO] Режим dry-run: задачи не будут созданы
[INFO] Найдено 3 //todo комментария

Задача 1:
  Заголовок: Добавить валидацию формы
  Файл: src/components/Form.js
  Строка: 42
  Приоритет: high
  Категория: feature
  Назначено: @john_doe
  Метки: good first issue, feature, high

Задача 2:
  Заголовок: Исправить стили кнопки
  Файл: src/styles/Button.css
  Строка: 15
  Приоритет: medium
  Категория: bug
  Метки: good first issue, bug, medium

Задача 3:
  [WARNING] Пропущена из-за дублирования
  Файл: src/utils/helper.js
  Строка: 10
  Описание: Оптимизировать функцию

[INFO] Завершено за 8.7 секунды
```

## 5. Нефункциональные требования

- **Безопасность**:
  - Хранение токена в переменных окружения или зашифрованном виде.
  - Проверка прав доступа перед созданием issue.
- **Логирование**:
  - Уровни: INFO, WARNING, ERROR, DEBUG.
  - Формат: `[LEVEL] <сообщение> (файл:строка, если применимо)`.
- **Платформа**: Linux.

## 6. Технологический стек

- **Язык**: Java 11+.
- **Тестирование**: `JUnit 5 (юнит-тесты)`, `Mockito` (моки для GitHub API и файловой системы).
- **CI/CD**: GitHub Actions.

