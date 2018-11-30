package it.ldsoftware.webfleet.api.v1.service

import it.ldsoftware.webfleet.api.v1.model.{DriverResult, Update}

trait UpdateDriverV1 {
  /**
    * Adds an update (blog entry) to the website.
    *
    * @param aggregate the aggregate's id where to insert the content
    * @param update    the update that must be created
    * @param jwt       the jwt token with the auth information
    * @return a DriverResult object for pattern matching
    */
  def addUpdate(aggregate: String, update: Update, jwt: String): DriverResult

  /**
    * Marks the update as published. If the user doesn't have the permission to publish the update, then
    * it is marked as pending review from a moderator.
    *
    * @param name the id of the update to publish
    * @param jwt  the jwt token with the auth information
    * @return a DriverResult object for pattern matching
    */
  def publishUpdate(name: String, jwt: String): DriverResult

  /**
    * Edits the update information
    *
    * @param name   the id of the update to publish
    * @param update the update information to be edited
    * @param jwt    the jwt token with the auth information
    * @return a DriverResult object for pattern matching
    */
  def editUpdate(name: String, update: Update, jwt: String): DriverResult

  /**
    * The review of an update is basically the same as editing, but checks different permissions on the
    * user requiring the operation.
    *
    * @param name   the id of the update to publish
    * @param update the update information to be edited
    * @param jwt    the jwt token with the auth information
    * @return a DriverResult object for pattern matching
    */
  def reviewUpdate(name: String, update: Update, jwt: String): DriverResult

  /**
    * Deletes an update
    *
    * @param name the id of the update to publish
    * @param jwt  the jwt token with the auth information
    * @return a DriverResult object for pattern matching
    */
  def deleteUpdate(name: String, jwt: String): DriverResult

  /**
    * Moves an update from an aggregate to another aggregate
    *
    * @param name the id of the update to publish
    * @param to   the id of the destination aggregate
    * @param jwt  the jwt token with the auth information
    * @return a DriverResult object for pattern matching
    */
  def moveUpdate(name: String, to: String, jwt: String): DriverResult

  /**
    * Rejects the publishing of an update, providing information on why the update has been rejected so
    * the user can either delete the update or review it.
    *
    * @param name   the id of the update to publish
    * @param reason the reason for rejection (mandatory)
    * @param jwt    the jwt token with the auth information
    * @return a DriverResult object for pattern matching
    */
  def rejectUpdate(name: String, reason: String, jwt: String): DriverResult

  /**
    * Sets the update as approved, with side effect of publishing it
    *
    * @param name the id of the update to publish
    * @param jwt  the jwt token with the auth information
    * @return a DriverResult object for pattern matching
    */
  def approveUpdate(name: String, jwt: String): DriverResult
}
