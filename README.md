# Mcleaner


> [!NOTE]  
> Some artifacts are expected, for example some trees might be cut inproperly read [here](https://github.com/aternosorg/thanos/issues/20)


Simple tool to optimize world size by removing inhabited chunks.
Works by checking how much a player has been in a chunk and deletes every chunk that is minor to a set threshold.

## HowTo?

> [!WARNING]  
> Make sure you create backups of your world before proceeding!

> [!TIP]  
> to optimize your server best as possible make sure you parse the normal world, the end, and the nether!

Download the jar file from the releases tab and simply double click it!

you can also run the jar with cli arguments, the first one being a path to the world folder second one being the time in seconds of min inhabited time.
## Compiling
run
``mvn package``
then built jar will be located in the "target" folder

Wow, that's literally it.

## License
this project is licensed under the [mit](https://opensource.org/license/mit) license.