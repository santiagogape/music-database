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


### request flow
1. grouping by 10s, each file song will be requested
2. visualizing the json, take your time to choose the correct data
   1. the data from the responses will be serialized as SpotifyTrackSearchResponse.java 
   2. there's to parts:
      1. on listView to choose the next track to identify
      2. a table view which shows all the SpotifyTrack from the response
         1. it will show a hyperlink with text as the track name for a url of the track 
         2. another for the album
         3. another for the first artist
         4. a button to select one and only one from the set of table entries
            1. once selected, the chosen SpotifyTrack will be stored in the same file, marking its entry in the Response Table with 
   3. in case the actual track isn't in the answer, 
      then you can choose to replace it for another json corresponding to a SpotifyTrack
3. after selecting the data, the json will be remade to fulfill the data model
4. once all the tracks are correct all the responses will be annalized once again to obtain all the artists and albums
5. the limit for several artists are 50, and the limit for several albums are 20.
   1. starting with the artists, in batches of 50, the database will be updated
   2. then albums in batches of 20
   3. once all the artists and albums are in the database, 
      then the database will be updated with the tracks with its relations




## flows

### read from files
either
+ temp
  + press the button
+ folder in the dbPath
  + press the button
  + open directoryChooser
    + if the directory isn't under dbPath then show warning to say that it won't be analysed
then:
  if:
    + the directory has already been read (Directories.Table)
  then:
    + redirect to the Tab database with said directory
  else:
    + read all files.
    + convert them to FileSong
    + show the list
### request
either:
+ read the list (after: read from files -> temp or folder)
+ read from database (in Tab database):
  either:
  + redirected from files:
    + a method must be called to pass the Directory that is to be read from
  + ask for the directory
  then:
  + once the directory is set:
    + search in the database the fileSong whose directory matches && don't have an associated entry in the Response.Table
then:
+ get the request string from all the fileSongs
    + start the batch request
        + a batch request groups batches of 10 request, then wait 20s for the next batch
        + it has a batchObserver so each response can be obtained sequentially and not at the end
            + each response will be stored as a json by the observer in case the operation must end
                + this includes the Response.Table to be updated
                  for the given FileSong whose request was used to get the request
                    + the response will be set to the defaults: "CHECKED"=false "ADDED"=false "Contained"=false
                      + if a response has "CHECKED" && "Contained", then the content of the json is a SpotifyTrack...
                      + and if it's "ADDED"=true then there's an entry in the Objects.Table and Tracks.Table
                      + else if "CHECKED"=true but "Contained"=false then it's a SpotifyTrackSearchResponse with items=[]
                        or the actual SpotifyTrack is not in the items.
                + the id will be the same as the fileSong, and its entry in Files.Table
            + at the same time will be converted to SpotifyTrackSearchResponse to be passed to another FXMLController onto a list
### selection
+ a listView to visualize the current FileSong
  either: [#processSelection]()
  + the SpotifyTrackSearchResponse associated with the FileSong.request() has "items": [#withItems]()
    + then a TableView will be set at the right of the listView
      + each entry represents a SpotifyTrack as the given:
        + hyperlink, text: name, url of track
        + hyperlink, text: album name, url of album
        + hyperlink, text: first artist name, url of first artist
        + button -> if pressed then that's the actual SpotifyTrack to be transformed into a SpotifyTrack
      + when a spotifyTrack is selected, then the SpotifyTrack will be stored in the JsonFile associated with the Response [#CheckProcess]()
        + updates the Response.Table for the entry to have "CHECKED"=true "Contained"=true
        + the spotifyTrack will be mapped to its FileSong like: FileSong -> Response with the same id() -> mapped with value SpotifyTrack 
          + [#mapResponseSpotifyTrack]()
        + then it will be time for the next response to be checked
          + the list will show with green the previous FileSong
          + it will be selected the following FileSong to the previous
          + back again to [#processSelection]()
      + BUT: there's a button to specify "the track is not contained in the response"
        + then said response will be treated as a one that has no items ->
  + it doesn't: [#skippedToBeAskedAfter]()
    + it will be skipped until the rest has ended, and stored temporary in another list 
      + also the database will be updated to say "CHECKED"=true, "Contained"=false
      + and marked as yellow in the list
### request 2 
both (asking user):
+ from [#withItems](): [#withSpotifyTracksProcess]()
  + using the [#mapResponseSpotifyTrack](), then obtains all the artists (simplifiedObject) and albums (simplified album)
    + storing in two lists: [#albumsAndartistsSets]()
      first:
        + a set of String for the album ids
          + while obtaining the artists ids to be added in the second list
          Second:
      + a set for the artists ids from the tracks
  then: [#SeveralLimits]() -> track=50, artists=50, albums=20
    + following the getSeveral... endpoints' limits: [#AlbumsAndArtistsMaps]()
      + in batches of 50 artists, requests to the getSeveralArtists endpoint
        + update the database Objects.Table, Artists.Table, Genres.Table
        + storing in a map(string id, SpotifyAlbum)
      + in batches of 20 albums, requests to the getSeveralAlbums endpoint
        + update the database Objects.Table, Albums.Table, ALBUM_ARTISTS.Table
        + storing in a map(string id, SpotifyArtist)
  then: [#AddTrack]()
    + for each Response key from [#mapResponseSpotifyTrack](), gets the SpotifyTrack value
    + now that first where added both album and artist:
      + updates database Objects.Table, Tracks.Table (with the id of the album that must be in Albums.Table),
        also Track_Artists.Table where both the artist and track are in the database
      + also updates the associated entry in Response.Table to have "ADDED"=true
+ from [#skippedToBeAskedAfter]():
  + using the other list, the user will be asked for an spotifyTrackID to be assigned to a Response [#responseToTrackIdMap]()
    + keep in mind that at this point, a given response will be "checked"=true,"added"=false,"contained"=false
    + updates database Temporary.Table(response id, track id)
    + IF the track is not in Spotify -> updates Particular.Table(response id)
  + after all ids are filled:
    + uses [#responseToTrackIdMap]() to call the endpoint getSeveralTracks in batches of 50 ids
      + follows partially [#checkProcess](): correcting the jsons, updating Responses.Table 
      + while eliminating the entry in Temporary.Table
    + then back to [#withSpotifyTracksProcess]() once all the batches are fulfilled

### not from spotify
a tab solely to add something that's not from spotify
+ manually add artist
+ add artist album (only if the artist exist)
+ add track (both artist and album exist)
  + first check the database for Particular.Table  -> then Files.Table && Responses.Table
    + if everything's correct, there's an entry in Files and Responses, 
      and in Responses the entry has "CHECKED"=true, "ADDED"=false, "Contained"=false
  + then individually, creates both Artist and Album if they don't exist
  + otherwise, get the artist by name, and add the album or get the album by name as well
  + then with those ids, add the track
    + updates Responses.table, objects.Table, tracks.table, artists.table, albums.table, album-artists.table, track-artists.table, genres.table
    + updates the jsons

## flow exceptions



