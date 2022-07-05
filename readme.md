# Tiny Economy Renewed

The purpose of this project is basically to create a small economy system for a private server.

This project uses a mariadb database provided by the mod [MariaDBServerFabric](https://github.com/AmibeSkyfy16/MariaDBServerFabricMC)


The following things are implemented :

1. Players can earn money by doing the following things
   - accomplishing advancements
   - killing entities
   - mining blocks
   
    
    Money rewards are defined in the database (by default, everything is 0)

    For the moment you have to configure the rewards through the code, or by editing the database with a software like HeidiSQL

    TODO: Decrease the rewards exponentially when a player kills several entities while staying in a given area from a given time.
          This is obviously to counter certain farms that generate a lot of entities

2. Players have the possibility to buy and sell items by creating their own shops

<img src="https://github.com/AmibeSkyfy16/Resources/blob/main/Images/shop.png?raw=true" alt="shop-image">