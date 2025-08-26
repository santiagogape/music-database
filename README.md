PROJECT APPS
---
# MUSIC
#### *local explorer and fancy UI for stored media (mp3)*

## tools:
- sort songs and customize UI
- use AI to make playlists on the spot with the current songs available through this app, including storing those lists
- karaoke (posible feature)
- backup and synchronization with local storages

---
# MUSIC DATABASE

## tools
- fill metadata using both title and artist using the spotifyAPI
- configures the structure of the database that references the files
- configures the folders and files according to the database

> Project structure: MVC

## MODULES

1. APP
   1. model
      + items, such as track, album, artist, etc
      + utilities, such as chronometer
   2. view
   3. control
      + manager of the token used for api calls
   4. io
      + rewrite metadata from mp3 files and images if needed
2. dependencies
   1. jaudiotagger
      + access and modification of metadata of mp3 files
   2. spotify
      + api calls and Json usage to transform responses into
        objects of the model
   3. sqlite
      + database initialization
      + controlled redundancy, references and relations 
        between archives, folders, and objects of the model
3. main
   + initializes process, obtains resources to launch the app. 

## PROCESS

1. view sets a window for the user to select the files to add to the database
2. said files will be analysed to get either filename or title, album, artist
3. obtained previous string, the app calls the spotify api for each
   1. if there's not an available token, one will be asked the api
   2. a token life span is that of 1h so 1min before it expires,
      a new one will be requested
4. treated as a stream, the data obtain will be stored in a temporal table,
   given that the api response includes many versions, that might not be the actual song search initially
5. the view will let you check the correct data, this won't be automatic,
   since only the user knows the real song, it is possible to compare it 
   using the link to the track from spotify in this point
6. once confirmed the selected data, the metadata from each file will be updated,
   and then, if it's needed, it will be moved to the correct location in the storage
7. after the files are moved to their final location, the database will be updated.
