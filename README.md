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

## notes for exiting
checking the folder jaudiotagger for the dependencies to create in App.java

## notes


## PROCESS

### setup
1. creates database if doesn't exists
2. creates each table
3. creates token manager (uses token request and provides access token on demand)
4. creates jaudiotagger to reed/write file's metadata
5. app launch (files tab, request tab, json chooser tab, data tab)

### flow
1. view sets a tab for the user to select the files to add to the database
   1. given a file, it will be used jaudiotagger to obtain a string called request:
      either filename or title + album + artist
   2. then it will be referenced as a FileSong which will be added to the database
   3. those FileSong will be stored temporally to then be given to the request tab
2. on the request tab, the database will be consulted to obtain FileSongs that doesn't have a response 
   (before the creation time the new files where added to the database) 
   and form a single list of FileSong to be requested to the spotifyAPI
   1. once started the request, the token manager must update the token before it expires (1h)
   2. for each response obtained from the api, a new Response will be made and updated in both the database and repository
      and each response will be associated with a FileSong of the same ID
   3. once the transaction is finished a follow-up window will open ->
3. the view will let you check the correct data, this won't be automatic,
   since only the user knows the real song, it is possible to compare it 
   using the link to the track from spotify in this point
   1. the Response has a path of the file to reference, which is the json from the response
   2. so said json will be converted to spotify items such as track
   3. once the correct SpotifyTrack is selected there will be more requests:
      + first checking if for each item, the id exist in the database as OBJECTS(spotify)
      1. for each of the artists of the track
      2. the one to get the album
      3. for each artist in the album 
4. once confirmed the selected data, the metadata from each file will be updated,
   and then, if it's needed, it will be moved to the correct location in the storage
5. after the files are moved to their final location, the database will be updated (commit).
