# Installer DBR Office

Les installateurs Windows et Linux sont générés par le workflow GitHub **Build desktop installers**. Chaque paquet inclut les bibliothèques Java de l’application et un runtime Java adapté au système cible. L’utilisateur n’a donc pas besoin d’installer Java séparément.

## Télécharger les installateurs

1. Ouvrir l’onglet **Actions** du dépôt GitHub `bellamard/dbrOffice`.
2. Sélectionner une exécution verte de **Build desktop installers**.
3. Télécharger `DBR-Office-Windows` pour Windows ou `DBR-Office-Linux` pour Linux.
4. Décompresser l’artefact téléchargé.

Le paquet Windows est un installateur `.exe`. Le paquet Linux est un paquet Debian `.deb`, destiné à Ubuntu 22.04 ou une distribution Debian compatible.

## Installer sous Windows

1. Ouvrir `DBR Office-1.0.0.exe`.
2. Choisir le dossier d’installation et terminer l’assistant.
3. Lancer **DBR Office** depuis le menu Démarrer ou le raccourci créé par l’installation.

L’installateur Windows nécessite Windows 10 ou une version ultérieure en 64 bits.

## Installer sous Linux

Sur Ubuntu ou Debian, ouvrir un terminal dans le dossier du paquet et lancer :

```bash
sudo apt install ./dbr-office_1.0.0-1_amd64.deb
```

Le paquet installe les bibliothèques système natives nécessaires via le gestionnaire de paquets et embarque le runtime Java ainsi que les bibliothèques applicatives. Lancer ensuite **DBR Office** depuis le menu des applications.

## Construire localement

La construction d’un installateur doit être exécutée sur le système visé : Windows pour `.exe`, Linux pour `.deb`. Il faut un JDK 17 et Maven (ou le wrapper Maven du dépôt). Sous Windows, le packaging `.exe` nécessite également WiX Toolset 3.14.

```powershell
$env:DBR_VERSION = "1.0.0"
./scripts/package-windows.ps1
```

```bash
DBR_VERSION=1.0.0 bash scripts/package-linux.sh
```

Les paquets sont écrits dans `dist/windows` et `dist/linux`.

## Limites de compatibilité

Le runtime Java et les bibliothèques de DBR Office sont intégrés aux paquets. Le paquet Linux déclare les bibliothèques natives de l’interface graphique comme dépendances Debian; `apt` les installe automatiquement. Il ne contient pas de copie de ces composants système, ni du noyau ou des composants de la distribution Linux.
