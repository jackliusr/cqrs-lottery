package com.xebia.cqrs.domain

import java.util.UUID;

import org.apache.commons.lang.Validate;

object VersionedId {
  val INITIAL_VERSION = 0L;
  private val LATEST_VERSION =  Math.MAX_LONG

  def random() = forInitialVersion(UUID.randomUUID())

  def forInitialVersion(id : UUID) = forSpecificVersion(id, INITIAL_VERSION)

  def forLatestVersion(id : UUID) = forSpecificVersion(id, LATEST_VERSION)

  def forSpecificVersion(id : UUID, version : Long) = new VersionedId(id, version)  
}

@serializable
@SerialVersionUID(1L)
case class VersionedId(
        val id : UUID,
        val version : Long
        ) extends ValueObject {
  Validate.notNull(id, "id is required");
  Validate.isTrue(version >= VersionedId.INITIAL_VERSION, "version must be greater than or equal to INITIAL_VERSION");

  def getId() = id;

  def getVersion() = version

  def isForInitialVersion() = (version == VersionedId.INITIAL_VERSION)

  def isForLatestVersion() = (version == VersionedId.LATEST_VERSION)

  def isForSpecificVersion() = !isForLatestVersion()

  def withVersion(version : Long) = VersionedId.forSpecificVersion(id, version)

  def nextVersion() = if (isForLatestVersion()) this else withVersion(version + 1)

  def equalsIgnoreVersion(other : VersionedId) : Boolean = {
    if (this == other) {
      return true;
    }
    if (other == null) {
      return false;
    }
    return id.equals(other.id);
  }

  def isCompatible(other : VersionedId) = {
    Validate.isTrue(other.isForSpecificVersion(), "cannot check for compatibility with non-specific version");
    if (isForLatestVersion()) equalsIgnoreVersion(other) else equals(other);
  }

  override def toString()  = if (isForLatestVersion()) id.toString() else id + "#" + version;
}
