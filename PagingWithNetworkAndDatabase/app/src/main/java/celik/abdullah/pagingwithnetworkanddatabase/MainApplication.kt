package celik.abdullah.pagingwithnetworkanddatabase

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/*
* All apps that use Hilt must contain an Application class that is annotated with @HiltAndroidApp.
* @HiltAndroidApp triggers Hilt's code generation, including a base class for your application
* that serves as the application-level dependency container.
* This generated Hilt component is attached to the Application object's lifecycle and provides
* dependencies to it. Additionally, it is the parent component of the app, which means that other
* components can access the dependencies that it provides.
* */
@HiltAndroidApp
class MainApplication: Application()