# EcoBudget 🌿

Dépôt de base pour le projet du cours de développement mobile avancé.

## Étapes de réalisation du projet

1. **Initialisation du dépôt**
   - Création du projet Android (Kotlin, Gradle Kotlin DSL) à partir du dépôt de base fourni par le professeur.
   - Configuration du `.gitignore` pour exclure les fichiers générés par l'IDE et le build (`.idea`, `.gradle`, `build/`, `local.properties`).

2. **Conception du modèle de données**
   - Définition de `Transaction` (id, titre, montant, type, catégorie, date, note) et `TransactionType` (`INCOME` / `EXPENSE`).
   - Définition de `Category` (énumération avec libellé et emoji) et `YearMonth` (mois sélectionné, navigation mois précédent/suivant).

3. **Mise en place de l'architecture MVVM**
   - Création de `TransactionRepository` (interface) et `FakeTransactionRepository` (implémentation en mémoire pour simuler les données) afin de découpler la source de données de la logique métier.
   - Création d'`EcoBudgetViewModel`, qui expose l'état de l'écran via un `StateFlow<EcoBudgetUiState>` combiné à partir du mois sélectionné, des transactions, des filtres de catégories et de l'état des dialogues.

4. **Extraction en module partagé Kotlin Multiplatform (`shared`)**
   - Création du module `shared` (plugin `kotlin("multiplatform")`) avec cibles `androidTarget` et iOS (`iosX64`, `iosArm64`, `iosSimulatorArm64`).
   - Déplacement des modèles, du repository et du `ViewModel` dans `commonMain` pour pouvoir réutiliser la même logique métier sur Android et iOS.
   - Ajout d'implémentations spécifiques par plateforme (`UUID.android.kt`, `UUID.ios.kt`) via le mécanisme `expect`/`actual`.
   - Ajout du module `shared` comme dépendance du module `app` (`implementation(project(":shared"))`).

5. **Logique métier du budget**
   - Calcul du revenu total, des dépenses totales et du solde à partir des transactions du mois affiché.
   - Calcul du taux d'utilisation du budget mensuel fixe (500 000 F CFA) et du budget restant.
   - Filtrage des transactions par catégorie sélectionnée.

6. **Construction de l'interface utilisateur (Jetpack Compose)**
   - `EcoBudgetScreen` : écran principal assemblant le résumé du budget, la navigation mensuelle et la liste des transactions.
   - `MonthNavigatorBar` : navigation entre les mois (précédent / suivant / retour au mois courant).
   - `TransactionCard` : affichage d'une transaction (titre, montant, catégorie, date).
   - `AddTransactionDialog` : formulaire d'ajout et de modification d'une transaction.
   - Thème de l'application (`Color.kt`, `Theme.kt`, `Type.kt`) pour une interface Material 3 cohérente.

7. **Câblage dans `MainActivity`**
   - Instanciation du `ViewModel` via `by viewModels()` et affichage d'`EcoBudgetScreen` dans un `Surface` avec le thème de l'application.

8. **Tests**
   - Ajout de tests unitaires (`ExampleUnitTest`, `ExampleRobolectricTest`) et d'un test instrumenté (`ExampleInstrumentedTest`) pour vérifier le bon fonctionnement des composants.

9. **Configuration du build et de la signature**
   - Ajout d'une configuration de signature (`signingConfigs`) pour le build de release, avec lecture du keystore et des mots de passe via variables d'environnement (`KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD`).

10. **Publication sur GitHub personnel**
    - Fork du dépôt d'origine du professeur sur un compte GitHub personnel.
    - Mise à jour du fork avec les modifications du projet (module `shared`, écrans, ViewModel) via `git add`, `git commit`, `git push`.

## Migration vers `commonMain` : neutralisation des dépendances plateforme

Pour chaque fichier déplacé dans `shared/src/commonMain`, le tableau ci-dessous documente le problème de compilation multiplateforme qui se serait posé avec une implémentation « naïve » (celle qu'on écrirait spontanément sur Android), le choix technique retenu pour neutraliser cette dépendance, et pourquoi ce choix est valide à la fois pour Android et iOS.

| Fichier | Problème rencontré | Choix technique appliqué | Justification multiplateforme |
|---|---|---|---|
| `model/Category.kt` | La version d'origine (module `app`) résolvait le libellé de chaque catégorie via `com.example.R.string.category_*` — un import Android (`com.example.R`) et un accès aux ressources (`Resources`/`Context`) totalement absents en `commonMain` et sur Kotlin/Native. | Remplacement des ID de ressources par des littéraux `String` Kotlin directement portés par l'énumération (`displayName: String`, `emoji: String`). | Une chaîne de caractères est un type du langage Kotlin lui-même : elle se compile à l'identique en bytecode JVM (Android) et en binaire Kotlin/Native (iOS), sans passer par un système de ressources propre à une plateforme. |
| `model/Transaction.kt` | Générer un identifiant unique et manipuler une date appellent naturellement `java.util.UUID.randomUUID()` et `java.time.Instant`/`System.currentTimeMillis()` — packages `java.*`/`java.time.*` indisponibles sur Kotlin/Native. | Représentation de la date via `kotlinx.datetime.Instant` (calculée depuis `dateMillis`) ; génération de l'identifiant déléguée à la fonction `expect fun generateUUID()` du module `util`. | `kotlinx.datetime` est la bibliothèque officielle JetBrains pour la date/heure en Kotlin Multiplatform : elle publie des binaires natifs pour chaque cible (Android, iOS) avec une API commune, garantissant un comportement identique sans code conditionnel par plateforme. |
| `model/YearMonth.kt` | Le calcul du mois courant et la navigation mois précédent/suivant reposeraient spontanément sur `java.time.YearMonth`/`java.time.LocalDate` et `Calendar`, JVM uniquement. | Reconstruction complète avec `kotlinx.datetime` : `Clock.System.now()`, `TimeZone.currentSystemDefault()`, `toLocalDateTime()`. | Ces API `kotlinx.datetime` sont conçues dès le départ pour être communes (`commonMain`) ; `Clock.System` et `TimeZone` ont chacun une implémentation native par plateforme fournie par la bibliothèque elle-même, donc aucune adaptation manuelle n'est nécessaire. |
| `util/UUID.kt` (+ `UUID.android.kt`, `UUID.ios.kt`) | `java.util.UUID` et `System.currentTimeMillis()` sont des API JVM (`java.util`) inexistantes sur Kotlin/Native ; il est donc impossible d'appeler directement ces fonctions depuis `commonMain`. | Déclaration de deux fonctions `expect` dans `commonMain` (`generateUUID()`, `getCurrentTimeMillis()`), avec une implémentation `actual` par cible : côté Android, `java.util.UUID.randomUUID()` et `System.currentTimeMillis()` (licites car `androidMain` s'exécute sur la JVM) ; côté iOS, un générateur d'UUID v4 écrit en Kotlin pur (`kotlin.random.Random`) et `kotlinx.datetime.Clock.System.now()`. | Le mécanisme `expect`/`actual` est l'outil prévu par Kotlin Multiplatform pour isoler une ligne de code réellement dépendante d'une plateforme derrière un contrat commun : tout le reste du module (`model`, `data`, `viewmodel`) ne connaît que la signature `expect`, jamais l'implémentation. Générer l'UUID iOS en Kotlin pur plutôt que d'appeler `platform.Foundation.NSUUID` évite en plus d'importer le framework Apple Foundation dans la logique métier partagée. |
| `util/DateUtils.kt` (`getTimeForMonth`) | Calculer « le jour J du mois M+décalage, à telle heure » nécessite normalement `java.time.LocalDate`/`Calendar.add`, indisponibles hors JVM. | Réécriture avec les types multiplateformes `kotlinx.datetime.LocalDate`, `LocalDateTime`, `DateTimeUnit.MONTH`, `Instant`, combinés à la fonction `getCurrentTimeMillis()` neutralisée ci-dessus. | Même bibliothèque, même garantie : l'arithmétique de dates (ajout de mois, conversion en timestamp) produit un résultat identique quel que soit le compilateur cible (JVM ou Kotlin/Native). |
| `data/repository/TransactionRepository.kt` / `FakeTransactionRepository.kt` | Le contrat réactif (`Flow`) ne posait pas de problème en soi, mais l'implémentation dépendait indirectement de génération d'ID et de dates — donc de tout ce qui précède. | Conservation de `kotlinx.coroutines.flow` (`Flow`, `MutableStateFlow`, `asStateFlow`) comme contrat réactif, et appel exclusif aux fonctions déjà neutralisées (`generateUUID()`, `getTimeForMonth()`) plutôt qu'aux API JVM directement. | `kotlinx-coroutines-core` publie également des binaires Kotlin/Native ; en ne s'appuyant que sur des dépendances elles-mêmes multiplateformes, le repository entier devient réutilisable tel quel par une vue iOS. |
| `viewmodel/EcoBudgetViewModel.kt` / `EcoBudgetUiState.kt` | `ViewModel` et `viewModelScope` proviennent habituellement de `androidx.lifecycle:lifecycle-viewmodel-ktx` (Google), une bibliothèque **Android uniquement** qui dépend du framework Android et ne peut pas être compilée en `commonMain`. | Remplacement de la dépendance par l'artefact multiplateforme publié par JetBrains, `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel` (déclaré `libs.androidx.lifecycle.viewmodel` dans `shared/build.gradle.kts`), qui republie la même API `ViewModel`/`viewModelScope` pour `commonMain` et iOS. | Ce choix ne change strictement rien au code appelant (`viewModelScope.launch { ... }`, `StateFlow<EcoBudgetUiState>`) : c'est un remplacement « à l'identique » de la dépendance, à la source même du problème, plutôt qu'un contournement — exactement l'approche attendue pour neutraliser une dépendance plateforme sans dénaturer l'architecture MVVM. |

## Analyse des erreurs de compilation rencontrées

Après l'extraction de la logique métier vers le module `shared`, le projet ne compilait plus pour le module `app`. Voici l'analyse des deux erreurs distinctes rencontrées, leur cause racine et leur résolution.

### Erreur 1 — Classes dupliquées entre `app` et `shared`

**Symptôme observé** (`./gradlew assembleDebug`, tâche `:app:compileDebugKotlin`) :

```
e: AddTransactionDialog.kt:84:65 Unresolved reference 'FOOD'.
e: AddTransactionDialog.kt:228:50 Unresolved reference 'displayName'.
e: TransactionCard.kt:65:45 Unresolved reference 'displayName'.
e: TransactionCard.kt:72:21 Unresolved reference 'dateMillis'.
e: EcoBudgetScreen.kt:182:49 Unresolved reference 'label'.
```

**Diagnostic.** Ces membres (`FOOD`, `displayName`, `dateMillis`, `label`) existaient pourtant bel et bien dans les classes du module `shared` (`shared/src/commonMain/kotlin/com/example/model/Category.kt`, `Transaction.kt`). En comparant les fichiers, la cause est apparue : d'anciennes versions de `Category`, `Transaction`, `YearMonth`, `TransactionRepository`, `FakeTransactionRepository` et `EcoBudgetViewModel` — héritées du dépôt de base, avant la migration vers Kotlin Multiplatform — étaient toujours présentes dans `app/src/main/java/com/example/{model,data,viewmodel}`, avec **le même nom de package et de classe** que leurs équivalents dans `shared`. Par exemple, l'ancienne `Category` ne définissait que 4 catégories liées à des ressources Android (`R.string.category_transport`, etc.) et n'avait ni `FOOD`, ni `displayName`, ni `label`.

Le compilateur Kotlin, lorsqu'il compile le module `app`, priorise les sources appartenant au module courant sur les classes binaires importées depuis une dépendance de projet (`implementation(project(":shared"))`). Résultat : les imports `com.example.model.Category` dans les écrans Compose résolvaient silencieusement vers l'**ancienne** classe locale du module `app`, pas vers celle, plus complète, du module `shared` — d'où des références « introuvables » sur des membres qui, pourtant, existaient bien quelque part dans le projet.

**Résolution.** Suppression des fichiers dupliqués dans `app/` (`git rm`), puisque leur contenu avait déjà été entièrement migré vers `shared/commonMain`. Le module `app` ne conserve plus que le code spécifique à l'UI Android (activités, composables, thème) et consomme les modèles/repository/ViewModel exclusivement via le module partagé — ce qui est justement l'objectif de la neutralité de domaine recherchée par l'architecture Kotlin Multiplatform.

### Erreur 2 — Faute de frappe sur l'opérateur `..`

**Symptôme** (après correction de l'erreur 1) :

```
e: AddTransactionDialog.kt:84:64 Unresolved reference 'rangeTo' for operator '..'.
e: AddTransactionDialog.kt:84:66 Unresolved reference 'FOOD'.
```

**Diagnostic.** La ligne en cause était :

```kotlin
mutableStateOf(initialTransaction?.category ?: Category..FOOD)
```

Le double point (`Category..FOOD`) n'est pas une erreur de compilation « profonde » mais une simple faute de frappe : Kotlin interprète `..` comme l'opérateur `rangeTo` (celui utilisé pour créer un intervalle, ex. `1..10`), et tente donc de créer un intervalle entre la référence de type `Category` et l'identifiant `FOOD` — ce qui n'a pas de sens et n'est défini nulle part. L'expression correcte, un accès simple à une valeur d'énumération, s'écrit avec un seul point : `Category.FOOD`.

**Résolution.** Correction du double point en simple point. Cette modification n'était présente que dans la copie de travail locale (jamais commitée), ce qui explique pourquoi le dernier commit du dépôt ne contenait déjà que la version correcte.

### Avertissements de build (non bloquants)

Un `./gradlew clean assembleDebug` fait apparaître deux avertissements informatifs, sans impact sur la compilation :

1. **Compatibilité AGP / Kotlin** : la version d'Android Gradle Plugin utilisée (8.10.1) est plus récente que la version maximale officiellement testée par le plugin Kotlin Multiplatform (8.5). Le build fonctionne correctement, mais Kotlin signale qu'il n'a pas validé cette combinaison précise.
2. **Cibles iOS désactivées** : les cibles `iosX64`, `iosArm64` et `iosSimulatorArm64` déclarées dans `shared/build.gradle.kts` sont automatiquement désactivées car Windows ne dispose pas des outils Apple (Xcode) nécessaires pour compiler du code natif iOS. Le code source `iosMain` n'est donc syntaxiquement validé par le compilateur Kotlin/Native que sur une machine macOS.

### Validation finale

- `./gradlew clean assembleDebug` → `BUILD SUCCESSFUL`, sans erreur ni avertissement bloquant.
- Application installée et lancée sur un émulateur Android (`adb install` + `adb shell am start`) : l'activité reste au premier plan, aucun crash relevé dans `logcat`, l'interface affiche correctement le budget, les catégories et la liste des transactions.
