# Backend For Frontend (BFF) Template
Starting point for ITMP BFF development.

## Table of Contents
- [Generating your repository](#generating-your-repository)
- [Structure of the repository](#Structure)
  - [github](#gitub)
  - [ci](#ci---continuous-integration)
  - [api](#api---api-module)
  - [module](#domain)
  - [applciation](#application)
  - [contract-tests](#contract-tests)

- [Code style](#Code-Style)
- [SonarQube](#SonarQube)
- [Build Tool](#Build-Tool)
- [deploy to Azure (playground)](#deploy-to-azure-playground)



## Generating your repository
[This is a template repository](https://docs.github.com/en/github/creating-cloning-and-archiving-repositories/creating-a-repository-from-a-template).
You can generate your new repository based on this and having this structure as a starting point by clicking `Use this template` button.

### Settings
On the following page select `it-master-platform` as owner, type in the repository name, choose `public` and click `Create repository from template`.
Now the repository has been created but there are some more `Settings` to be adjusted.

#### Options - Merge Button
- check "Allow squash merging" and "Automatically delete head branches"
- disable "Allow merge commits" and "Allow rebase merging"

#### Collaborators & teams
- add team "BFF" with "Admin" access
- create or add project team with "Write" access

#### Branches
Add a branch protection rule for `master` with the following options checked:
- `Require pull request reviews before merging`
- `Require status checks to pass before merging` (the PR status check is available after it ran one time)
- `Require branches to be up to date before merging`
- `Include administrators`
- `Restrict who can push to matching branches`

#### Hooks
add `https://jenkins-adp-tools-service-owners.apps.crp.ec1.aws.aztec.cloud.allianz/github-webhook/` (json, all events)


## Structure

### .github
Github specific files. Have a smooth github experience with predefined templates.
- Issue templates - Bug_report, Feature_request 
- CODEOWNERS - Manage your code effectively. Setup teams, assign PRs to them automatically. 
- pull_request_template - Same PR description with the most important part.  

### ci - Continuous Integration
- CI related files: Job DSL groovy script, Jenkinsfile
- Read more about the [Jenkins Groovy Library](https://github.developer.allianz.io/gdf/Jenkins_Build_Groovy_Lib)

### api - API module
- API's definition. The API definition schema (Swagger, GraphQL) can be placed here, so it can be shared independently of code. 
- Swagger example. [Open API](https://swagger.io/specification/) definition and [Swagger-u](https://swagger.io/tools/swagger-ui/).
NPM API package as an independent artifact to share it with Frontend eg: for MockServer purpose.  
 
### domain - domain logic
An API domain module which represents a well-defined API section. 
EG: vehicle, offering, contract. 

### application
Spring Boot Application which contains the API domain modules.

### contract tests
See [docs](contract-tests/)
## Code-Style
To have the same coding experience on Eclipse and IntelliJ the same code formatting should be used.
Import the settings to your IDE accordingly. 
The code style configurations are based on the [Spring Framework code style](https://github.com/spring-projects/spring-framework/tree/master/src).

## SonarQube
SonarQube is a platform for continuous inspection of code quality to perform automatic reviews with static analysis of code to detect bugs, code smells, and security vulnerabilities.
It can be configured via `sonar-project.properties` and found [here](https://sonarqube-adp-tools-service-owners.apps.crp.ec1.aws.aztec.cloud.allianz/projects)

## Build-Tool
Maven is used as the build tool for services. 
You can find the Maven wrapper in the root so no need to install it. 



## deploy to Azure (playground) 
If you would like to deploy your app to kubernetes and test it on the Allianz network, 
you can deploy directly to Allianz ITMP Dev AKS cluster. 
#### (background explanation)
following the guide https://docs.microsoft.com/en-us/azure/developer/java/spring-framework/deploy-spring-boot-java-app-on-kubernetes,
For that I have added a `playground` namespace . 

In order to create a docker image , I have added the `jib-maven-plugin` and pointed it to our ACR 
using the `docker.image.prefix` property

As we already have ACR and AKS in place, all is left is to use `jib` to build the image on ACR and 
use `kubectl` to run the pod on AKS.

### perquisites:
- user privilege for ACR and AKS ()
- `kubectl` installed
- docker cli installed (no need for docker daemon)

### connect to the ACR (Azure container regitry)
```shell script
TOKEN=$(az acr login -n azitmpweaztecprodsharedacr --expose-token | jq -r '.accessToken' )
docker login azitmpweaztecprodsharedacr.azurecr.io --username 00000000-0000-0000-0000-000000000000 -p $TOKEN
```
### build docker image and push
```shell script
cd application
mvn compile jib:build
```
### login to Azure K8s 
```shell script
az account set --subscription 6b8236f0-f43e-4182-88b6-29a2d47d8aee
az aks get-credentials --resource-group rg-itmp-nprd-frontend-we1-d-main-akscluster --name aks-itmp-nprd-frontend-we1-d-main
```

### RUN
```shell script
kubectl run bff-template-spring-boot --image=azitmpweaztecprodsharedacr.azurecr.io/itmp-bff-template:latest --namespace playground
```
Wait until available:
```shell script
kubectl wait --for=condition=ready --timeout=30s   po bff-template-spring-boot --namespace playground
kubectl get po --namespace playground 
```
You now should see your pod running! 
It should look like this:
```shell script
NAME                       READY   STATUS    RESTARTS   AGE
bff-template-spring-boot   1/1     Running   0          26m
```
If it fails to start, you must check out the logs:
```shell script
kubectl logs  bff-template-spring-boot --namespace playground
```
fix and retry...

### Expose your pod (DOES NOT WORK BECAUSE THERE IS A FIREWALL)
~~
Expose
```shell script
kubectl expose pod bff-template-spring-boot --namespace playground --type=LoadBalancer --port=80 --target-port=8080
```
wait few seconds......

Get the public IP of your BFF!
```shell script
kubectl get services -o=jsonpath='{.items[*].status.loadBalancer.ingress[0].ip}' --namespace playground
```
~~

### Access your pod
We will open a terminal into the pod.
Then you can curl it happily :-)
```shell script
kubectl exec bff-template-spring-boot  --namespace playground -it -- /bin/sh
/ # apk --no-cache add curl
.
.
.
/ # curl localhost:8080/actuator
```

### delete
Clean up after yourself!
```shell script
k delete po bff-template-spring-boot --namespace playground 

```
### TO DO 
-  clean image too
