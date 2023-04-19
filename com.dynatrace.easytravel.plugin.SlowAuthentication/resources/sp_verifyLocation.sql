USE [easytravel]
GO

/****** Object:  StoredProcedure [dbo].[sp_verifyLocation]    Script Date: 2014-10-27 15:02:54 ******/
DROP PROCEDURE [dbo].[sp_verifyLocation]
GO

/****** Object:  StoredProcedure [dbo].[sp_verifyLocation]    Script Date: 2014-10-27 15:02:54 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO



CREATE PROCEDURE [dbo].[sp_verifyLocation]
	@location int
 
AS
BEGIN
	declare @t as datetime
	select @t=DATEADD(millisecond,@location,0)

    WAITFOR DELAY @t    
END     
GO


