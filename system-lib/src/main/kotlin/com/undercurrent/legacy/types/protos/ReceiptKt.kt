//Generated by the protocol buffer compiler. DO NOT EDIT!
// source: external.proto

package com.undercurrent.legacy.types.protos;

import com.undercurrent.legacy.types.protos.MobileCoinAPI.Receipt

@kotlin.jvm.JvmName("-initializereceipt")
inline fun receipt(block: ReceiptKt.Dsl.() -> kotlin.Unit): Receipt =
   ReceiptKt.Dsl._create(Receipt.newBuilder()).apply { block() }._build()
object ReceiptKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  class Dsl private constructor(
    private val _builder: Receipt.Builder
  ) {
    companion object {
      @kotlin.jvm.JvmSynthetic
      @kotlin.PublishedApi
      internal fun _create(builder: Receipt.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
    internal fun _build(): Receipt = _builder.build()

    /**
     * <pre>
     * Public key of the TxOut.
     * </pre>
     *
     * <code>.external.CompressedRistretto public_key = 1;</code>
     */
    var publicKey: com.undercurrent.legacy.types.protos.MobileCoinAPI.CompressedRistretto
      @JvmName("getPublicKey")
      get() = _builder.getPublicKey()
      @JvmName("setPublicKey")
      set(value) {
        _builder.setPublicKey(value)
      }
    /**
     * <pre>
     * Public key of the TxOut.
     * </pre>
     *
     * <code>.external.CompressedRistretto public_key = 1;</code>
     */
    fun clearPublicKey() {
      _builder.clearPublicKey()
    }
    /**
     * <pre>
     * Public key of the TxOut.
     * </pre>
     *
     * <code>.external.CompressedRistretto public_key = 1;</code>
     * @return Whether the publicKey field is set.
     */
    fun hasPublicKey(): kotlin.Boolean {
      return _builder.hasPublicKey()
    }

    /**
     * <pre>
     * Confirmation number of the TxOut.
     * </pre>
     *
     * <code>.external.TxOutConfirmationNumber confirmation = 2;</code>
     */
    var confirmation: com.undercurrent.legacy.types.protos.MobileCoinAPI.TxOutConfirmationNumber
      @JvmName("getConfirmation")
      get() = _builder.getConfirmation()
      @JvmName("setConfirmation")
      set(value) {
        _builder.setConfirmation(value)
      }
    /**
     * <pre>
     * Confirmation number of the TxOut.
     * </pre>
     *
     * <code>.external.TxOutConfirmationNumber confirmation = 2;</code>
     */
    fun clearConfirmation() {
      _builder.clearConfirmation()
    }
    /**
     * <pre>
     * Confirmation number of the TxOut.
     * </pre>
     *
     * <code>.external.TxOutConfirmationNumber confirmation = 2;</code>
     * @return Whether the confirmation field is set.
     */
    fun hasConfirmation(): kotlin.Boolean {
      return _builder.hasConfirmation()
    }

    /**
     * <pre>
     * Tombstone block of the Tx that produced the TxOut.
     * Note: This value is self-reported by the sender and is unverifiable.
     * </pre>
     *
     * <code>uint64 tombstone_block = 3;</code>
     */
    var tombstoneBlock: kotlin.Long
      @JvmName("getTombstoneBlock")
      get() = _builder.getTombstoneBlock()
      @JvmName("setTombstoneBlock")
      set(value) {
        _builder.setTombstoneBlock(value)
      }
    /**
     * <pre>
     * Tombstone block of the Tx that produced the TxOut.
     * Note: This value is self-reported by the sender and is unverifiable.
     * </pre>
     *
     * <code>uint64 tombstone_block = 3;</code>
     */
    fun clearTombstoneBlock() {
      _builder.clearTombstoneBlock()
    }

    /**
     * <code>.external.MaskedAmount masked_amount_v1 = 4;</code>
     */
    var maskedAmountV1: com.undercurrent.legacy.types.protos.MobileCoinAPI.MaskedAmount
      @JvmName("getMaskedAmountV1")
      get() = _builder.getMaskedAmountV1()
      @JvmName("setMaskedAmountV1")
      set(value) {
        _builder.setMaskedAmountV1(value)
      }
    /**
     * <code>.external.MaskedAmount masked_amount_v1 = 4;</code>
     */
    fun clearMaskedAmountV1() {
      _builder.clearMaskedAmountV1()
    }
    /**
     * <code>.external.MaskedAmount masked_amount_v1 = 4;</code>
     * @return Whether the maskedAmountV1 field is set.
     */
    fun hasMaskedAmountV1(): kotlin.Boolean {
      return _builder.hasMaskedAmountV1()
    }

    /**
     * <code>.external.MaskedAmount masked_amount_v2 = 5;</code>
     */
    var maskedAmountV2: com.undercurrent.legacy.types.protos.MobileCoinAPI.MaskedAmount
      @JvmName("getMaskedAmountV2")
      get() = _builder.getMaskedAmountV2()
      @JvmName("setMaskedAmountV2")
      set(value) {
        _builder.setMaskedAmountV2(value)
      }
    /**
     * <code>.external.MaskedAmount masked_amount_v2 = 5;</code>
     */
    fun clearMaskedAmountV2() {
      _builder.clearMaskedAmountV2()
    }
    /**
     * <code>.external.MaskedAmount masked_amount_v2 = 5;</code>
     * @return Whether the maskedAmountV2 field is set.
     */
    fun hasMaskedAmountV2(): kotlin.Boolean {
      return _builder.hasMaskedAmountV2()
    }
    val maskedAmountCase: Receipt.MaskedAmountCase
      @JvmName("getMaskedAmountCase")
      get() = _builder.getMaskedAmountCase()

    fun clearMaskedAmount() {
      _builder.clearMaskedAmount()
    }
  }
}
@kotlin.jvm.JvmSynthetic
inline fun com.undercurrent.legacy.types.protos.MobileCoinAPI.Receipt.copy(block: ReceiptKt.Dsl.() -> kotlin.Unit): com.undercurrent.legacy.types.protos.MobileCoinAPI.Receipt =
   ReceiptKt.Dsl._create(this.toBuilder()).apply { block() }._build()

val com.undercurrent.legacy.types.protos.MobileCoinAPI.ReceiptOrBuilder.publicKeyOrNull: com.undercurrent.legacy.types.protos.MobileCoinAPI.CompressedRistretto?
  get() = if (hasPublicKey()) getPublicKey() else null

val com.undercurrent.legacy.types.protos.MobileCoinAPI.ReceiptOrBuilder.confirmationOrNull: com.undercurrent.legacy.types.protos.MobileCoinAPI.TxOutConfirmationNumber?
  get() = if (hasConfirmation()) getConfirmation() else null

val com.undercurrent.legacy.types.protos.MobileCoinAPI.ReceiptOrBuilder.maskedAmountV1OrNull: com.undercurrent.legacy.types.protos.MobileCoinAPI.MaskedAmount?
  get() = if (hasMaskedAmountV1()) getMaskedAmountV1() else null

val com.undercurrent.legacy.types.protos.MobileCoinAPI.ReceiptOrBuilder.maskedAmountV2OrNull: com.undercurrent.legacy.types.protos.MobileCoinAPI.MaskedAmount?
  get() = if (hasMaskedAmountV2()) getMaskedAmountV2() else null

