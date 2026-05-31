# BeautyMatch — Подбор тонального крема и пудры по тону кожи

[![Android](https://img.shields.io/badge/Android-8.0%2B-brightgreen)](https://developer.android.com)
[![Java](https://img.shields.io/badge/Java-1.8-blue)](https://www.java.com)
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)

**BeautyMatch** — Android-приложение для определения тона кожи с помощью камеры и подбора наиболее подходящих оттенков тональных средств, пудр и консилеров из встроенной базы данных.  
Проект создан в учебных целях и демонстрирует работу с **Camera2 API**, обработкой изображений, **SQLite**, фильтрацией данных и современным Android UI (Material Design, Glassmorphism).

---

## 📱 Возможности

- **Сканирование кожи** через камеру  
  - Переключение между задней и фронтальной камерой  
  - Включение/отключение вспышки (для задней камеры)  
  - Автофокус и нормализация цвета (опционально)

- **Интерактивный анализ**  
  - Добавление до 3 точек на фотографии (лоб, щёки, нос, подбородок или вручную)  
  - Усреднение цвета в области радиусом 10 пикселей  
  - Определение подтона кожи:  
    - **Тёплый** (Warm) – золотистый/персиковый  
    - **Холодный** (Cool) – розовый/оливковый  
    - **Нейтральный** (Neutral) – сбалансированный

- **Каталог оттенков**  
  - Просмотр всех продуктов (foundation / powder / concealer)  
  - Фильтрация по:  
    - типу продукта  
    - подтону  
    - покрытию (light / medium / full)  
    - финишу (matte / dewy / satin / natural)  
    - брендам (MAC, L’Oréal, Maybelline, Estée Lauder, Revlon, NARS)  
    - составу (SPF, отсутствие силиконов)  
  - Автоматический подбор 20 ближайших оттенков по евклидову расстоянию в RGB

- **Детальная карточка оттенка**  
  - Характеристики (подтон, покрытие, финиш, тип)  
  - Кросскоды – аналоги в других брендах

- **Экспорт результатов**  
  - Выгрузка текущего списка оттенков в CSV-файл  
  - Резервное копирование базы данных

- **Настройки**  
  - Управление вспышкой, автофокусом, нормализацией цвета  
  - Просмотр и сброс путей хранения (фото, БД, экспорт)  
  - Статистика базы данных (количество брендов, оттенков, кросскодов)  
  - Очистка кэша и экспорт БД

---

## 🖼 Скриншоты

> **Примечание:** Добавьте реальные скриншоты в папку `screenshots/` и раскомментируйте строки ниже.

<!--
| Главное меню | Анализ тона | Каталог | Фильтры |
|--------------|-------------|---------|---------|
| ![Main](screenshots/main.png) | ![Analysis](screenshots/analysis.png) | ![Catalog](screenshots/catalog.png) | ![Filters](screenshots/filters.png) |
-->

---

## 🛠 Технологии и библиотеки

| Компонент            | Технология / Библиотека                         |
|----------------------|--------------------------------------------------|
| Язык                 | Java 1.8                                         |
| Минимальная версия   | Android API 26 (Android 8.0)                    |
| Target SDK           | 31 (Android 12)                                 |
| UI                   | XML, ConstraintLayout, CardView, RecyclerView   |
| Камера               | Camera2 API (с поддержкой устаревших методов)   |
| База данных          | SQLite (встроенная, предзаполненная)            |
| Графика              | Bitmap, Matrix, ImageReader, custom drawables   |
| Файловая система     | FileProvider, внешнее хранилище (Android 10+)   |
| Асинхронность        | HandlerThread, backgroundHandler                |

**Проект не использует** CameraX, Room, Gson, OkHttp или Material3 – реализован на чистом Android SDK.

---

## 📁 Структура проекта

```

app/src/main/java/com/beauty/match/
├── MainActivity.java                    # Главное меню
├── camera/
│   ├── CameraActivity.java              # Съёмка (Camera2)
│   ├── AutoFitTextureView.java          # TextureView с авто-масштабированием
│   └── Camera2LegacyHelper.java         # Хелпер для устаревших API
├── analysis/
│   └── SkinToneActivity.java            # Анализ фото: точки, цвет, подтон
├── catalog/
│   ├── ProductListActivity.java         # Список оттенков + экспорт
│   ├── FilterActivity.java              # Активность фильтров
│   └── ShadeAdapter.java                # Adapter для RecyclerView
├── settings/
│   └── SettingsActivity.java            # Настройки приложения
├── database/
│   └── DatabaseHelper.java              # SQLiteOpenHelper + тестовые данные
├── model/
│   ├── Brand.java
│   ├── Product.java
│   └── Shade.java
└── utils/
    ├── ColorUtils.java                  # Расстояние между цветами, подтон, нормализация
    ├── ImageUtils.java                  # Поворот, отражение, сохранение Bitmap
    └── StorageUtils.java                # Работа с папками, размер, копирование

```

Ресурсы (`res/`):
- `layout/` – все активности (activity_*.xml) и элементы списков (item_*.xml)
- `drawable/` – иконки, фоны, градиенты, селекторы
- `anim/` – анимации появления (fade_in, slide_up)
- `values/` – цвета, строки, размеры, темы

---

## 🚀 Установка и сборка


1. **Клонируйте репозиторий**
```bash
   git clone https://github.com/yourusername/BeautyMatch.git
   cd BeautyMatch
```
2. Откройте проект в Android Studio (рекомендуется Arctic Fox или новее)
3. Соберите APK
   · Подключите Android-устройство или эмулятор с API 26+
   · Нажмите Run (зелёный треугольник) или выберите Build > Build Bundle(s) / APK(s) > Build APK(s)
4. Разрешения
      При первом запуске приложение запросит разрешения на камеру и чтение/запись внешнего хранилища. Предоставьте их для полной функциональности.

Для Android 11+ может потребоваться ручное разрешение на управление внешним хранилищем (в настройках системы). В приложении реализован механизм запроса legacy-доступа.

---

## 📖 Использование

1. Сканирование кожи

· На главном экране нажмите «Сканировать кожу»
· Сделайте фото (можно переключиться на фронтальную камеру)

2. Добавление точек анализа

· Коснитесь области чистой кожи на фото (лоб, щёки, подбородок)
· Либо используйте кнопки быстрых областей
· Максимум 3 точки

3. Анализ

· Нажмите «Анализировать»
· Приложение покажет средний цвет, подтон кожи и описание

4. Подбор продуктов

· Нажмите «Подобрать тональные средства» – откроется каталог с оттенками, отсортированными по близости к вашему тону
· Также можно вручную зайти в «Каталог оттенков» и применить фильтры

5. Просмотр деталей

· Нажмите на любой оттенок в списке – появится диалог с характеристиками и аналогами (кросскодами)

6. Экспорт

· В каталоге нажмите на иконку экспорта (ic_export)
· CSV-файл сохранится в папку экспорта (см. Настройки)

---

## 📄 Лицензия

Проект распространяется под лицензией MIT. Вы можете свободно использовать, модифицировать и распространять код при условии сохранения уведомления об авторстве.


MIT License

Copyright (c) 2025 BeautyMatch Contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.


---

## 🙏 Благодарности

· Данные брендов и оттенков являются тестовыми и не претендуют на коммерческую точность.
· Проект выполнен в учебных целях как демонстрация навыков Android-разработки (Java, Camera2, SQLite, UI/UX).

---

Автор: dimazzz, KsandrSkif 

GitHub: https://github.com/dimazzz01

Если у вас есть вопросы или предложения, создавайте Issue или пишите на почту.

⭐ Не забудьте поставить звезду, если проект оказался полезным!
