https://console.cloud.google.com/home/dashboard
1. Create project, remember the project id (mine is joj-experiment)
2. Getting Started/Enable APIs and get credentials like keys
3. Google cloud APIS -> Credentials -> 
    Configure OAuth consent screen (fill in Product Name) (fix a privacy policy: https://www.freeprivacypolicy.com/free-privacy-policy-generator.php)
    Create Credentials

#Kubernetes cluster
#Short version of https://cloud.google.com/kubernetes-engine/docs/quickstart

#Install gcloud kubernetes
gcloud components install kubectl

#Sets which project and which zone to use (https://cloud.google.com/compute/docs/regions-zones/#available)
gcloud config set project malmo-improv
gcloud config set compute/zone europe-west3-a

gcloud container clusters create malmo-improv #Creates a cluster named malmo-improv

gcloud container clusters get-credentials malmo-improv #Get auth credentials for interacting with the kubernetes cluster on google cloud

kubectl run hello-server --image gcr.io/google-samples/hello-app:1.0 --port 8080 #Uses the image gcr.io/google-samples/hello-app and runs it as a hello-server service

kubectl expose deployment hello-server --type "LoadBalancer" #expose your application

kubectl get service hello-server #get status for the hello-server service

kubectl delete service hello-server #deletes the service hello-server

gcloud container clusters delete malmo-improv #Delete the whole cluster

#Short version of https://cloud.google.com/container-registry/docs/quickstart

docker build -t quickstart-image . #Build docker image

docker run quickstart-image #Run a local docker container, Adding -p 50050:80 would publish port 80 in the container as 50050 on the host

docker tag quickstart-image gcr.io/malmo-improv/quickstart-image:tag1 #Tagging the Docker image with a registry name configures the docker push command to push the image to a specific location

gcloud auth configure-docker #auth docker with google cloud (only once)

http://gcr.io/malmo-improv #link to published docker images

docker push gcr.io/malmo-improv/quickstart-image:tag1 #Publish the docker image to gcr.io (or https://docs.docker.com/docker-cloud/builds/push-images/)

docker pull gcr.io/malmo-improv/quickstart-image:tag1 #Download docker image

gcloud container images delete gcr.io/malmo-improv/quickstart-image:tag1 #remove it from google cloud

#Local Kubernetes
https://www.virtualbox.org/wiki/Downloads
https://github.com/kubernetes/minikube/releases
minicube start
kubectl create -f basic-pod.yaml
kubectl port-forward nginx 60700:80

kubectl create secret generic my-secret --from-literal=key1=supersecret --from-literal=key2=topsecret
kubectl get secret my-secret -o yaml