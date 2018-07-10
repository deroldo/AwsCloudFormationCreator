FROM java:8-jre-alpine

ENV APP_HOME "/opt/app"

ADD build/libs/*.jar $APP_HOME/app.jar

CMD	java -jar \
    -DUSER_DATA=$USER_DATA \
    -DGIT_HASH=$GIT_HASH \
    -DAWS_FILE=$AWS_FILE \
    -DAWS_PUBLISH=$AWS_PUBLISH \
    -DAWS_REGION=$AWS_REGION \
    -DSTACK_NAME=$STACK_NAME \
    -DAWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID \
    -DAWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY \
    /opt/app/app.jar