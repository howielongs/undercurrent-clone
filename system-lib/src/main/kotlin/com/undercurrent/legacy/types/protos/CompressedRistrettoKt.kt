//Generated by the protocol buffer compiler. DO NOT EDIT!
// source: external.proto

package com.undercurrent.legacy.types.protos;

@kotlin.jvm.JvmName("-initializecompressedRistretto")
inline fun compressedRistretto(block: CompressedRistrettoKt.Dsl.() -> kotlin.Unit): com.undercurrent.legacy.types.protos.MobileCoinAPI.CompressedRistretto =
  CompressedRistrettoKt.Dsl._create(com.undercurrent.legacy.types.protos.MobileCoinAPI.CompressedRistretto.newBuilder())
    .apply { block() }._build()
object CompressedRistrettoKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  class Dsl private constructor(
    private val _builder: com.undercurrent.legacy.types.protos.MobileCoinAPI.CompressedRistretto.Builder
  ) {
    companion object {
      @kotlin.jvm.JvmSynthetic
      @kotlin.PublishedApi
      internal fun _create(builder: com.undercurrent.legacy.types.protos.MobileCoinAPI.CompressedRistretto.Builder): Dsl =
        Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
    internal fun _build(): com.undercurrent.legacy.types.protos.MobileCoinAPI.CompressedRistretto =
      _builder.build()

    /**
     * <code>bytes data = 1;</code>
     */
    var data: com.google.protobuf.ByteString
      @JvmName("getData")
      get() = _builder.getData()
      @JvmName("setData")
      set(value) {
        _builder.setData(value)
      }
    /**
     * <code>bytes data = 1;</code>
     */
    fun clearData() {
      _builder.clearData()
    }
  }
}
@kotlin.jvm.JvmSynthetic
inline fun com.undercurrent.legacy.types.protos.MobileCoinAPI.CompressedRistretto.copy(block: CompressedRistrettoKt.Dsl.() -> kotlin.Unit): com.undercurrent.legacy.types.protos.MobileCoinAPI.CompressedRistretto =
   CompressedRistrettoKt.Dsl._create(this.toBuilder()).apply { block() }._build()

