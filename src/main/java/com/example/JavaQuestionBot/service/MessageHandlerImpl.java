package com.example.JavaQuestionBot.service;

public class MessageHandlerImpl implements MessageHandler{
    private TelegramBot bot;
    private Command command;

    private static final String UNKNOWN_COMMAND = "Извините, я не знаю такой команды.";

    public MessageHandlerImpl(TelegramBot bot) {
        this.bot = bot;
        command = new CommandImpl(bot);
    }

    @Override
    public void messageProcessing(String message, String userName, Long chatId) {
        switch (message) {
            case "/start":
            case "/старт":
                command.startCommandReceived(chatId, userName);
                break;
            case "/help":
                command.helpCommandReceived(chatId, userName);
                break;
            case "/question":
                if(bot.getPersonalDataFoUsers().get(chatId).getCurrentCategory() == null){
                    command.askCurrentCategory(chatId);
                    break;
                }
                bot.getPersonalDataFoUsers().get(chatId).setCurrentDBRow();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {

                }
                command.askQuestion(chatId, bot.getPersonalDataFoUsers().get(chatId));
                break;
            case "/category":
                if(bot.getPersonalDataFoUsers().get(chatId).getQuestions() != null) {
                    bot.sendMessage(chatId, "В данным момент установлена тема: " + bot.getPersonalDataFoUsers().get(chatId).getCurrentCategory().getName() +
                            "\nХотите поменять тему?");
                }
                command.askCurrentCategory(chatId);
                break;
            case "/currenttheme":
                bot.sendMessage(chatId, "Текущая тема вопросов: \n" + bot.getPersonalDataFoUsers().get(chatId).getCurrentCategory().getName());
                break;
            default: bot.sendMessage(chatId, UNKNOWN_COMMAND);
        }
    }
}
