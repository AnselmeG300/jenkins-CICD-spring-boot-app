## A. Paramétrage et exécution de l'application en local

Afin de mieux comprendre la construction de l'image, nous allons faire ce que l'image docker est censée faire, mais en local dans un premier temps, c'est à dire: construire et exécuter l'applciation.

### 1/ Installer le JDk et maven sous la VM Ubuntu

```bash 
sudo apt install openjdk-17-jre-headless maven

```


### 2/ Construction de l'application

```bash 
mvn clean install
```
Cette commande va produire l'exécutable de l'application [target/paymybuddy.jar](target/paymybuddy.jar)



### 3/ Démarrer et initialiser la base de données 

#### i. Démarrez un container mysql

```bash 
sudo docker run --name mysql-paymybuddy -p 3306:3306 -e MYSQL_ROOT_PASSWORD=password -d mysql
```

#### ii. Puis vérifiez qu'il a bien démarré en vous connectant à ce dernier: 

```bash
mysql -h <ip_docker0> -u root -p
```

Remplacez **ip_docker0** par l'adresse IP de l'interface  `docker0` obtenue grâce à `ifconfig`

Entrez le mot de passe `password` à la demande du mot de passe, vous devriez avoir accès au prompt:  

 `mysql>`

Faites `CTRL+D` pour sortir


#### iii. Initialisez la base de données de l'application

Déplacez-vous dans le répertoire [src/main/resources/database](src/main/resources/database)

```bash
cd src/main/resources/database
```
et exécutez les deux fichiers sql comme suit : 

```bash 
mysql -h <ip_docker0> -u root -p < create.sql
mysql -h <ip_docker0> -u root -p < data.sql
```


### 4/ Démarrage de l'application

#### i. Exporter les variables d'environnement nécessaires pour la connexion de l'application à la BD

```bash 
export SPRING_DATASOURCE_USERNAME=root

export SPRING_DATASOURCE_PASSWORD=password

export SPRING_DATASOURCE_URL=jdbc:mysql://<ip_docker0>:3306/db_paymybuddy

```

#### ii. Exécuter l'application

```bash 
java -jar target/pymybuddy.jar
```

En ouvrant dans le navigateur [localhost:8080](localhost:8080) vous avez l'application !


____


Nous allons nous faciliter la tâche en exécutant les étapes 1 et  4  dans un container.

  

## B. Construction du fichier dockerfile

### 1/ Installer JAVA dans le container

Créez un fichier nommé `Dockerfile` à la racine du projet et y inclure les instructions suivantes commençant par *#dockerfile* , une à une.


Notre image s'appuie sur une image légère contenant le nécessaire pour l'exécution du code JAVA : `amazoncorretto:17-alpine`

```Dockerfile
#dockerfile

FROM amazoncorretto:17-alpine
```

  


### 2/ Construction de l'application 

Nous n'allons pas installer maven dans le container car l'étape de construction maven (`mvn clean install`) doit être faite avant la construction du container. Sinon l'image mettrait trop de temps à être construite et serait lourde, ce qui va à l'encontre du principe de légèreté des containers.

Cette étape restera manuelle ( ou automatisée par l'outil d'intégration continue).
Rappelez-vous de faire `mvn clean install` avant chaque construction de l'image.

Idem A-2

### 3/ Démarrer et initialiser la BD

Idem A-3




### 4/ Démarrage de l'application

#### i. Copier le jar dans le container

```Dockerfile
#dockerfile

ARG JAR_FILE=target/blog.jar

WORKDIR /app

COPY ${JAR_FILE} paymybuddy.jar

ENV SPRING_DATASOURCE_USERNAME=root

ENV SPRING_DATASOURCE_PASSWORD=password

ENV SPRING_DATASOURCE_URL=jdbc:mysql://<ip_docker0>:3306/db_paymybuddy
```

`ARG JAR_FILE=target/blog.jar` déclare une variable disponible uniquement lors de la construction de l'image dont la valeur est le chemin vers l'exécutable de l'application

`COPY ${JAR_FILE} paymybuddy.jar` copie l'exécutable de la machine hôte vers le répertoire de travail sous le nom paymybuddy.jar. Le container contiendra alors le fichier: /app/paymybuddy.jar

Avec ENV nous déclarons les variables d'env nécessaires au démarrage de l'application.

 
#### ii. Démarrer la construction de l'image 

Le contenu final du fichier Dockerfile devrait être le suivant:

``` 
FROM amazoncorretto:17-alpine

ARG JAR_FILE=target/paymybuddy.jar

WORKDIR /app

COPY ${JAR_FILE} paymybuddy.jar

ENV SPRING_DATASOURCE_USERNAME=root

ENV SPRING_DATASOURCE_PASSWORD=password

ENV SPRING_DATASOURCE_URL=jdbc:mysql://<ip_docker0>:3306/db_paymybuddy

CMD ["java", "-jar" , "paymybuddy.jar"]
```

```bash
#terminal à la racine du projet

sudo docker build -t paymybuddy:latest .
```


#### iii. Instantier l'image

```bash 
#terminal 

docker run --name paymybuddy -p 8080:8080 paymybuddy:latest
```


En ouvrant dans le navigateur [localhost:8080](localhost:8080) vous avez l'application !




