package io.hoplin;

/**
 * Error handler strategy
 */
public interface ConsumerErrorStrategy
{
   /**
    * Handle consumer error
    *
    * @param context the context the error is attached to
    * @param throwable the exception that we try to handle
    * @return
    */
   default AckStrategy handleConsumerError(final MessageContext context, final Throwable throwable)
   {
      return AcknowledgmentStrategies.NACK_WITH_REQUEUE.strategy();
   }

   /**
    * Handle message cancellation
    * We NACK current message with requeue set to true
    *
    * @param context the message that we trying to cancel
    * @return
    */
   default AckStrategy handleConsumerCancelled(final MessageContext context)
   {
      return AcknowledgmentStrategies.NACK_WITH_REQUEUE.strategy();
   }

}
