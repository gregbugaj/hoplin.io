package hoplin.io;

public interface ConsumerErrorStrategy
{
   /**
    * Handle consumer error
    *
    * @param context the context the error is attached to
    * @param throwable the exception that we try to handle
    * @return
    */
   default AckStrategy handleConsumerError(final ConsumerExecutionContext context, final Throwable throwable)
   {
      return AcknowledgmentStrategies.NACK_WITH_REQUEUE.strategy();
   }

   /**
    * Handle message cancellation
    *
    * @param context
    * @return
    */
   default AckStrategy handleConsumerCancelled(final ConsumerExecutionContext context)
   {
      return AcknowledgmentStrategies.NACK_WITH_REQUEUE.strategy();
   }

}
