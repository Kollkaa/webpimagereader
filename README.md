# WebP/VP8 decoder

Mainly built for decoding Lossy WebP compression. WebP and Vp8 both use the same method for decoding frames.

So, vp8decoder is used for webp image reading.

Refer to riff/webp specs from Google: https://developers.google.com/speed/webp/docs/riff_container

Refer to WebPImageReaderSpi that reads the image using javax.imageio.

Also, there are unit tests written for testing the webp image reader.

#### NOTE: This is a reader ONLY, you would need to write a WriteSpi to work with Imageio
