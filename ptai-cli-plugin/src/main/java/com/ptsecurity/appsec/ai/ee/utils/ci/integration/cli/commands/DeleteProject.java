package com.ptsecurity.appsec.ai.ee.utils.ci.integration.cli.commands;

import com.ptsecurity.appsec.ai.ee.utils.ci.integration.Resources;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.cli.Plugin;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.cli.commands.BaseCommand.ExitCode;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.domain.ConnectionSettings;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.domain.PasswordCredentials;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.domain.Reports;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.domain.TokenCredentials;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.exceptions.GenericException;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.jobs.AbstractJob;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.jobs.DeleteProjectJob;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.jobs.ListReportTemplatesJob;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.utils.CallHelper;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import static com.ptsecurity.appsec.ai.ee.utils.ci.integration.jobs.AbstractJob.JobExecutionResult.SUCCESS;
import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@CommandLine.Command(
        name = "delete-project",
        sortOptions = false,
        description = "Delete one or more PT AI project",
        exitCodeOnInvalidInput = Plugin.INVALID_INPUT,
        exitCodeListHeading = "Exit Codes:%n",
        exitCodeList = {
                "0:Success",
                "1:Failure",
                "1000:Invalid input"})
public class DeleteProject extends BaseCommand implements Callable<Integer> {
    public static class Project {
        @CommandLine.Option(
                names = {"--project-name"}, required = true, order = 6,
                paramLabel = "<name>",
                description = "PT AI project name")
        protected String projectName;

        @CommandLine.Option(
                names = {"--project-id"}, required = true, order = 6,
                paramLabel = "<id>",
                description = "PT AI project ID")
        protected UUID projectId;

        @CommandLine.Option(
                names = {"--project-name-regexp"}, required = true, order = 6,
                paramLabel = "<expression>",
                description = "Regular expression that used to search for PT AI project name")
        protected String regexp;
    }

    @CommandLine.ArgGroup
    protected Project project;

    @CommandLine.Option(
            names = {"-y", "--yes", "--assume-yes"}, order = 98,
            description = "Automatic yes to prompts; assume \"yes\" as answer to all prompts and run non-interactively")
    protected boolean yes = false;

    @Slf4j
    @SuperBuilder
    public static class CliDeleteProjectJob extends DeleteProjectJob {
        protected Path truststore;
        @Override
        protected void init() throws GenericException {
            String caCertsPem = (null == truststore)
                    ? null
                    : CallHelper.call(
                    () -> {
                        log.debug("Loading trusted certificates from {}", truststore.toString());
                        return new String(Files.readAllBytes(truststore), UTF_8);
                    },
                    Resources.i18n_ast_settings_server_ca_pem_message_file_read_failed());
            connectionSettings.setCaCertsPem(caCertsPem);
            super.init();
        }
    }

    protected DeleteProjectJob.DeleteConfirmationStatus confirm(final boolean singleProject, @NonNull final String name, @NonNull final UUID id) {
        String res;
        if (singleProject)
            res = System.console().readLine("Are you sure you want to delete PT AI project %s (id: %s) [y/N]?", name, id);
        else
            res = System.console().readLine("Are you sure you want to delete PT AI project %s (id: %s) [y/N/a]?", name, id);
        return res.trim().equalsIgnoreCase("y")
                ? DeleteProjectJob.DeleteConfirmationStatus.YES
                : res.trim().equalsIgnoreCase("a")
                ? DeleteProjectJob.DeleteConfirmationStatus.ALL
                : DeleteProjectJob.DeleteConfirmationStatus.NO;
    }

    @Override
    public Integer call() {
        CliDeleteProjectJob job = CliDeleteProjectJob.builder()
                .console(System.out)
                .prefix("")
                .verbose(verbose)
                .connectionSettings(ConnectionSettings.builder()
                        .insecure(insecure)
                        .url(url.toString())
                        .credentials(credentials.getBaseCredentials())
                        .build())
                .truststore(truststore)
                .projectId(project.projectId)
                .projectName(project.projectName)
                .expression(project.regexp)
                .confirmation(yes ? null : this::confirm)
                .build();
        AbstractJob.JobExecutionResult res = job.execute();
        return SUCCESS == res ? ExitCode.SUCCESS.getCode() : ExitCode.FAILED.getCode();
    }
}
