# CRDT P2P Collaborative Text Editor
Приложение для совместного редактирования текста на основе бесконфликтных реплицируемых типов данных с объединением пиров в одноранговую сеть через сигнальный сервер.

Jar-файлы для запуска сигнального сервера и пира [приложены](/jar/) в репозитории.

Для работы редактора сначала на одном из узлов нужно запустить сигнальный сервер, а затем приложение пира для подключения.

### Запуск сигнального сервера
Для запуска сервера нужно прописать команду:
```
java -jar signalserver.jar 
```
В появившемся окне указать: 
* IP-адрес, на котором будет работать сервер;
* пароль, если необходимо ограничить доступ в лобби (в противном случае оставить пустым);
* свободный порт для запуска сервера (либо оставить поле пустым, свободный порт будет выбран автоматически).
### Запуск редактора
Для запуска редактора нужно прописать команду:
```
java -jar peer.jar 
```
В появившемся окне указать: 
* IP-адрес сигнального сервера;
* порт сигнального сервера;
* IP-адрес своего компьютера;
* имя, под которым Вас увидят остальные участники;
* пароль, если необходимо.

Редактор работает с файлами текстового формата (.css, .html, .txt и т. д.).