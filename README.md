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


## Deployment
### API 
./gradlew clean build
docker builder prune
docker buildx build --no-cache --platform=linux/amd64 -t gcr.io/numeric-pilot-432704-n6/task-scheduler-api:v1 -f Dockerfile.api .
docker push gcr.io/numeric-pilot-432704-n6/task-scheduler-api:v1
gcloud run services replace service.yaml --region us-central1

### Scheduler
docker buildx build --no-cache --platform=linux/amd64 -t gcr.io/numeric-pilot-432704-n6/task-scheduler-job:v1 -f  Dockerfile.job .
docker push gcr.io/numeric-pilot-432704-n6/task-scheduler-job:v1
gcloud run jobs replace job.yaml --region=us-central1
gcloud run jobs execute task-scheduler-job --region=us-central1 