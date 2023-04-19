GO

/****** Object:  StoredProcedure [dbo].[sp_findJourneys]    Script Date: 01/11/2012 14:38:53 ******/
IF  EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[sp_findJourneys]') AND type in (N'P', N'PC'))
DROP PROCEDURE [dbo].[sp_findJourneys]
GO

USE [easytravel]
GO

/****** Object:  StoredProcedure [dbo].[sp_findJourneys]    Script Date: 01/11/2012 14:38:53 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO


CREATE PROCEDURE [dbo].[sp_findJourneys]
 @destination varchar(255),
    @fromDate datetime,
    @toDate datetime
AS
BEGIN
 SET NOCOUNT ON;

    declare @N as int
    declare @t as datetime
    
    select @N= COUNT(*) from Journey where destination_name = @destination 
       and fromDate >= @fromDate and toDate <= @toDate
       
 select @t=DATEADD(millisecond,@N*200,0)

    WAITFOR DELAY @t
    
 -- SET NOCOUNT ON added to prevent extra result sets from
 -- interfering with SELECT statements.

    select * from Journey where destination_name = @destination
        and fromDate >= @fromDate and toDate <= @toDate
END     

GO