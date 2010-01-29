package com.xebia.cqrs.bus

trait Bus {
  @throws(classOf[MessageHandlingException])
  def send(message: AnyRef);

  /**
   * Sends a message to all registered handlers and returns the <em>first</em>
   * response.
   *
   * @return the first response.
   * @exception MessageHandlingException
   *                an error occurred during message processing or no response
   *                was returned.
   */
  @throws(classOf[MessageHandlingException])
  def sendAndWaitForResponse(message: AnyRef): Response;

  /**
   * Reply to the sender of the current message with the specified message.
   */
  @throws(classOf[MessageHandlingException])
  def reply(message: AnyRef);

  /**
   * Reply to the sender of the current message with the specified messages.
   */
  @throws(classOf[MessageHandlingException])
  def reply(messages: Seq[AnyRef]);

  /**
   * Publishes a message to all subscribers.
   *
   * @exception MessageHandlingException
   *                an error occurred during message processing.
   */
  @throws(classOf[MessageHandlingException])
  def publish(message: AnyRef);

  /**
   * Publishes the messages to all subscribers. The events should be treated
   * as a single unit-of-work.
   *
   * @exception MessageHandlingException
   *                an error occurred during message processing.
   */
  @throws(classOf[MessageHandlingException])
  def publish(messages: Seq[AnyRef]);

  /**
   * The current message being processed.
   *
   * @return null if no message is being processed.
   */
  def currentMessage : AnyRef;

}
