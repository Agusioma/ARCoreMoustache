# ARCoreMoustache
A test project on adding a moustache to a face using Google's AR Core

The ARScene is rendered using the `[ARFragment](https://developers.google.com/sceneform/reference/com/google/ar/sceneform/ux/ArFragment)`. This fragement class is extended to create a custom fragment(`CustomArFragment`) where the sessions and the necessary configurations are declared. Using an XML view, the fragment is inflated from where the buttons needed for the Texture changes are rendered.
The `MainActivity` class is used to host the fragment, the views, the texture rendering, and the augmented faces set up.
The project uses PNG images files for the AR and not a model. 
