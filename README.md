# Oasis API

Backend API pour l'application Oasis, développé avec Spring Boot 4.0 et Kotlin.

## Technologies

- **Langage** : Kotlin
- **Framework** : Spring Boot 4.0
- **Base de données** : PostgreSQL
- **Build tool** : Maven
- **Java** : 17+

## Prérequis

Avant de commencer, assure-toi d'avoir installé :

- [JDK 17+](https://adoptium.net/)
- [Maven](https://maven.apache.org/) (ou utilise le wrapper `./mvnw`)
- [Docker](https://www.docker.com/) et Docker Compose
- [IntelliJ IDEA](https://www.jetbrains.com/idea/) (recommandé)

## Configuration locale

### 1. Cloner le projet
```bash
git clone https://github.com/jeansaigne/oasis-api.git
cd oasis-api
```

### 2. Lancer la base de données PostgreSQL

Le projet inclut un fichier `docker-compose.yml` pour lancer une instance PostgreSQL locale.
```bash
docker-compose up -d
```

Cela démarre un conteneur PostgreSQL avec la configuration suivante :

| Paramètre | Valeur |
|-----------|--------|
| Host | localhost |
| Port | 5432 |
| Database | oasisdb_bph7 |
| User | oasisdbuser |
| Password | oasisdbpassword |

**Commandes utiles :**
```bash
# Vérifier que le conteneur tourne
docker-compose ps

# Voir les logs
docker-compose logs -f postgres

# Arrêter la base de données
docker-compose down

# Arrêter et supprimer les données
docker-compose down -v
```

### 3. Lancer l'application

#### Avec Maven
```bash
./mvnw spring-boot:run
```

#### Avec IntelliJ IDEA

1. Ouvre le projet dans IntelliJ
2. Attends que Maven importe les dépendances
3. Lance la classe `OasisApiApplicationKt`

### 4. Vérifier que tout fonctionne

Ouvre ton navigateur et accède à :
```
http://localhost:8080/hello
```

Tu devrais voir : **Hello World**

## Structure du projet
```
oasis-api/
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/oasisplatform/oasisapi/
│   │   │       ├── controller/
│   │   │       │   └── HelloController.kt
│   │   │       └── OasisApiApplication.kt
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       ├── kotlin/
│       │   └── com/oasisplatform/oasisapi/
│       │       └── OasisApiApplicationTests.kt
│       └── resources/
│           └── application-test.yml
├── docker-compose.yml
├── pom.xml
└── README.md
```

## Tests

Les tests utilisent une base de données H2 en mémoire.
```bash
# Lancer tous les tests
./mvnw test
```

## Endpoints disponibles

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/hello` | Endpoint de test retournant "Hello World" |

## Configuration

### Variables d'environnement

L'application peut être configurée via les variables d'environnement suivantes :

| Variable | Description | Valeur par défaut |
|----------|-------------|-------------------|
| PORT | Port du serveur | 8080 |
| DB_HOST | Hôte de la base de données | localhost |
| DB_PORT | Port de la base de données | 5432 |
| DB_NAME | Nom de la base de données | oasisdb_bph7 |
| DB_USER | Utilisateur de la base de données | oasisdbuser |
| DB_PASSWORD | Mot de passe de la base de données | oasisdbpassword |

## Déploiement

L'application est configurée pour un déploiement automatique sur [Render](https://render.com/).

### Base de données de production

- **Host** : dpg-d4q3vak9c44c73b62g60-a.frankfurt-postgres.render.com
- **Database** : oasisdb_bph7
- **Region** : Frankfurt (Europe)

## Contribuer

1. Crée une branche pour ta feature : `git checkout -b feature/ma-feature`
2. Commit tes changements : `git commit -m "Ajout de ma feature"`
3. Pousse la branche : `git push origin feature/ma-feature`
4. Ouvre une Pull Request

## Licence

Ce projet est privé.