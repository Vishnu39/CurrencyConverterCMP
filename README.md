# Currency Converter app

This app is a small test app, built with Compose Multiplatform. It supports Android, iOS, and desktop (checked on a Mac), with unit tests.

# App uses a simple UI

As there are a lot of currencies, I added pagination of 30 with a Show more option, to keep it usable on a low-end device.

I planned to add refresh, but the free API only has one snapshot per day, so there was no point. In the future it would have been an addition.

I haven't added an offline feature. If I had more time, I would have :)

## What the screen does

Two currency fields, From and To. Each one opens a dialog with search and a list of 30.

There is a result field which updates dynamically 

Default currencies are Euro to INR with value set default to 1

## Choices

One shared screen runs on Android, iOS, and desktop. That is why this is Kotlin Multiplatform.

The network calls use Ktor. Android uses OkHttp. iOS uses Darwin. Desktop uses Java. Retrofit would not run on iOS.

One ViewModel holds the screen state. The screen only draws that state and sends taps back.

There is no Hilt. `ConverterViewModelFactory` builds the HTTP client, the API, and the repository by hand.

The amount accepts a comma or a dot, because a German keyboard types `1,5`. The result is a `Double` shown with 2 to 4 decimals. `BigDecimal` does not work in the shared iOS code.

Night mode follows the phone setting.

The currency list is long, so the dialog has a search box and shows 30 rows at a time.