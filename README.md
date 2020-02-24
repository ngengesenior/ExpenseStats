## Expense Stats
Expense Stats is an Android app built with the aim of getting SMS sent by various banks and Mobile
Money operators in Cameroon. 

## What the App does currently
At the moment, the implementation of the app is such that it gets all SMS sent by Ecobank Cameroon to an Ecobank
customer and filters all messages that have the user's current balance and plots a chat of the users balance against date and time.
With this, the user can visualize how their balance fluctuates over time

Presently, here is how the app main app screens look

### Splash Screen

![Splash](screenshots/splash.png)

### MainActivity
![Main Screen](screenshots/main.png)


## Stack
The app is written entirely in Kotlin and uses [AnyChart Android](https://github.com/AnyChart/AnyChart-Android) for plotting the graph.

## Work in Progress
The app is a work in progress and the next feature is observing the pattern of MTN Mobile Money messages and getting the balances at each instance and plotting. Same will be done for Orange Money and other banks.
Just create a pull request if you want to contribute to the work or want a feature to be added.