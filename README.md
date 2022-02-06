# Quill Java

The purpose of this project is to implement the composition of Quill Deltas in Java.
It was initially developed as part of a project called [DownScribble](https://www.downscribble.com/) to enable
autosave, since some documents could be quite large. This would allow just the Delta object to be sent
to the back end, where they could be composed and persisted.

At any rate, if you're running Java on the back end, this may prove to be a useful way to keep 
the pipes a little cleaner.

I'd certainly welcome contributions if anyone finds something missing. I've only implemented the
portions needed by DownScribble.

And as a final note, the code is basically a line-for-line translation of the code in the [Delta](https://quilljs.com/docs/delta/)
project. As of this writing, DownScribble runs version 4.2.2 of delta, so this is the version from which
this library has been translated and tested.
