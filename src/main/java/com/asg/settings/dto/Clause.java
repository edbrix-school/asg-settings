package com.asg.settings.dto;

import java.util.List;

public record Clause(String sql, List<Object> params) {}