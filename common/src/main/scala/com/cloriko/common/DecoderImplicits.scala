package com.cloriko.common

import com.cloriko.protobuf.protocol.{ MasterRequest, SlaveResponse }
import com.google.protobuf.GeneratedMessage

object DecoderImplicits {

  case class OperationDescriptior(id: String, username: String, slaveId: String)

  def getMasterRequestDescriptor(masterRequest: MasterRequest): OperationDescriptior = {
    val sealedValue = masterRequest.sealedValue
    sealedValue.update match {
      case Some(update) => OperationDescriptior(update.id, update.username, update.slaveId)
      case _ =>
        sealedValue.delete match {
          case Some(delete) => OperationDescriptior(delete.id, delete.username, delete.slaveId)
          case _ =>
            sealedValue.fetchRequest match {
              case Some(fileRequest) => OperationDescriptior(fileRequest.id, fileRequest.username, fileRequest.slaveId)
              case _ =>
                sealedValue.overviewRequest match { //todo update
                  case Some(overviewRequest) => OperationDescriptior(overviewRequest.id, overviewRequest.username, "fakeSlaveId")
                  case _ => OperationDescriptior("fakeId", "fakeUsername", "fakeSlaveId")
                }
            }
        }
    }
  }

  case class ExtendedMasterRequest(masterRequest: MasterRequest) {
    private val descriptor = getMasterRequestDescriptor(masterRequest)
    val id: String = descriptor.id
    val username: String = descriptor.username
    val slaveId: String = descriptor.slaveId
    def asProto = masterRequest
  }

  implicit def extendMasterRequest(masterRequest: MasterRequest): ExtendedMasterRequest = {
    new ExtendedMasterRequest(masterRequest)
  }

  def decodeSlaveResponse(slaveResponse: SlaveResponse): OperationDescriptior = {
    val sealedValue = slaveResponse.sealedValue
    sealedValue.updated match {
      case Some(updated) => OperationDescriptior(updated.id, updated.username, updated.slaveId)
      case _ =>
        sealedValue.deleted match {
          case Some(deleted) => OperationDescriptior(deleted.id, deleted.username, deleted.slaveId)
          case _ =>
            sealedValue.fetchResponse match {
              case Some(fetchResponse) => OperationDescriptior(fetchResponse.id, "fakeUsername", "fakeSlaveId")
              case _ =>
                sealedValue.overviewResponse match {
                  case Some(overviewResponse) => OperationDescriptior(overviewResponse.id, "", "fakeSlaveId")
                  case _ => OperationDescriptior("fakeId", "fakeUsername", "fakeSlaveId")
                }
            }
        }
    }
  }

  case class ExtendedSlaveResponse(slaveResponse: SlaveResponse) {
    private val descriptor = decodeSlaveResponse(slaveResponse)
    val id: String = descriptor.id
    val username: String = descriptor.username
    val slaveId: String = descriptor.slaveId
    def asProto = slaveResponse
  }

  implicit def extendSlaveResponse(slaveResponse: SlaveResponse): ExtendedSlaveResponse =
    new ExtendedSlaveResponse(slaveResponse)

  implicit def updateAsMasterRequest(update: com.cloriko.protobuf.protocol.Update): ExtendedMasterRequest =
    new ExtendedMasterRequest(MasterRequest(MasterRequest.SealedValue.Update(update)))
  implicit def updatedAsSlaveResponse(updated: com.cloriko.protobuf.protocol.Updated): ExtendedSlaveResponse =
    new ExtendedSlaveResponse(SlaveResponse(SlaveResponse.SealedValue.Updated(updated)))

  implicit def deleteAsMasterRequest(delete: com.cloriko.protobuf.protocol.Delete): ExtendedMasterRequest =
    new ExtendedMasterRequest(MasterRequest(MasterRequest.SealedValue.Delete(delete)))
  implicit def deletedAsSlaveResponse(deleted: com.cloriko.protobuf.protocol.Deleted): ExtendedSlaveResponse =
    new ExtendedSlaveResponse(SlaveResponse(SlaveResponse.SealedValue.Deleted(deleted)))

  implicit def fetchRequestAsMasterRequest(fetchRequest: com.cloriko.protobuf.protocol.FetchRequest): ExtendedMasterRequest =
    new ExtendedMasterRequest(MasterRequest(MasterRequest.SealedValue.FetchRequest(fetchRequest)))
  implicit def fetchResponseAsSlaveResponse(fetchResponse: com.cloriko.protobuf.protocol.FetchResponse): ExtendedSlaveResponse =
    new ExtendedSlaveResponse(SlaveResponse(SlaveResponse.SealedValue.FetchResponse(fetchResponse)))

}
