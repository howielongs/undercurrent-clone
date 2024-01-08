package com.undercurrent.prompting.views

//todo Next: break down each piece into buildable components (line by line), save FileAttachment
//
//object ShopHtmlGenerator {
//    fun saveToImage(
//        logoPath: String = "htmlgen/alien-logo.png",
//        qrPath: String = "htmlgen/sample-qr-shop.png",
//        vararg attachmentPaths: String) {
//
//        val userhome = "user.home"
//        var testAttachmentPath: String = "/tmp/signal_attachments"
//        var resourcesPath = "src/main/resources/htmlgen"
//
//        val path = "$testAttachmentPath/shop-gen.png"
//
//        try {
//            File(path).delete()
//            println("Successfully deleted file at $path")
//        } catch (e: Exception) {
//            println("Exception when attempting to delete $path:\n${e.stackTraceToString()}")
//        }
//
//        var qrUrl = try {
//            var outUrl = File("$resourcesPath/sample-qr-shop.png").toURI().toURL()
//            println("Found file URL: $outUrl")
//            outUrl
//        } catch (e: Exception) {
//            println("ERROR loading QR image file")
//            null
//        }
//
//        val imageGenerator = HtmlImageGenerator()
//        var outHtml = File("$resourcesPath/vendor-menu-with-css.html").readText()
//
//        qrUrl?.let { outHtml = outHtml.replace("QR_IMG_PATH", it.toString()) }
//
//        imageGenerator.loadHtml(outHtml)
//        println("Generating image")
//
//        imageGenerator.saveAsImage(path)
//    }
//
//    var shopHtml = """
//        <section class="page-wrapper">
//            <div class="container">
//                <div class="row">
//                    <div class="col-lg-4">
//                        <img src="images/alien-logo.png" alt="Logo"
//                             width="100" ,
//                             height="100"
//                        />
//                        <h2 class="mb-4">Alien Bob's Vibe Emporium</h2>
//
//                        <p>To browse: send "17116733770" to using Signal +18049930648 (standard SMS not supported)
//                        </p>
//                        <!--				<p>Join my shop! COPY & PASTE this entire message or send code "17116733770" to "https://signal.me/#p/+18049930648" (+18049930648) in the Signal Messenger app.-->
//                        <!--				</p>-->
//                        <p>Last updated: August 8, 2022</p>
//                        <img src="images/sample-qr-shop.png" alt="Logo"
//                             width="100" ,
//                             height="100"
//                        />
//
//                    </div>
//                    <div class="col-lg-8 mt-5 mt-lg-0">
//                        <div class="pl-0 pl-lg-4">
//                            <h2 class="mb-3">Welcome to Alien Bob's Vibe Emporium</h2>
//                            <p>We aim to please with our therapeutic products. All-natural ingredients, never using sweatshop
//                                labor (unless absolutely necessary, I mean, we have a business to run here).</p>
//                            <h4>JUICEBOX</h4>
//                            <p>apple flavored
//                                <br>[1.1] ${'$'}22 / 64-pack<br>
//                                [1.2] ${'$'}23.44 / 12-pack
//                            </p>
//                            <h4>COPIER</h4>
//                            <p>
//                                Very fast
//                                <br> [2.1] ${'$'}23.33 / biggish
//                            </p>
//                            <h4>BOBSLED</h4>
//                            <p>
//                                Rosebud
//                                <br> [3.1] ${'$'}26.85 / Single
//                            </p>
//                            <h4>PEPPERS</h4>
//                            <p>
//                                Spicy
//                                <br>[4.1] ${'$'}2.99 / Red bell
//                            </p>
//                        </div>
//                    </div>
//                </div>
//            </div>
//        </section>
//    """.trimIndent()
//
//
//}