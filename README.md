# Simple Scheduler

## API
Creating a Task

```
curl -X POST -v -H "Content-Type: application/json" \\
-d '{"task_name": "My New Task",\n    "task_schedule": "* * * * *", \\
    "expiry": "2024-12-31T23:59:59Z",\n    "status": "SCHEDULED"\n  }' \\
https://task-scheduler-961662087251.us-central1.run.app/ \\
```
Fetch All Tasks

```https://task-scheduler-961662087251.us-central1.run.app/tasks```
